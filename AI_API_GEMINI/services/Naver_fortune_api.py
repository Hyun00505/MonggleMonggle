from __future__ import annotations

import logging
import os
import traceback
import threading
import atexit
from dataclasses import dataclass
from datetime import datetime, date
from pathlib import Path
from queue import Queue, Empty
from typing import Literal, Optional, Dict, Any

from fastapi import FastAPI, HTTPException
from pydantic import BaseModel, Field
from selenium import webdriver
from selenium.common.exceptions import (
    TimeoutException,
    NoSuchElementException,
    StaleElementReferenceException,
    ElementClickInterceptedException,
    WebDriverException,
)
from selenium.webdriver.chrome.service import Service
from selenium.webdriver.common.by import By
from selenium.webdriver.common.keys import Keys
from selenium.webdriver.support import expected_conditions as EC
from selenium.webdriver.support.ui import WebDriverWait
from webdriver_manager.chrome import ChromeDriverManager


# ==================== 운세 캐싱 시스템 ====================
class FortuneCache:
    """
    운세 캐싱 시스템
    같은 날짜 + 같은 생년월일의 운세는 하루 동안 동일하므로 캐싱
    """
    def __init__(self):
        self._cache: Dict[str, Any] = {}
        self._lock = threading.Lock()
    
    def _make_key(self, gender: str, calendar_type: str, birth_date: str) -> str:
        """캐시 키 생성 (오늘 날짜 포함)"""
        today = date.today().isoformat()
        return f"{gender}_{calendar_type}_{birth_date}_{today}"
    
    def get(self, gender: str, calendar_type: str, birth_date: str) -> Optional[Any]:
        """캐시에서 운세 조회"""
        key = self._make_key(gender, calendar_type, birth_date)
        with self._lock:
            if key in self._cache:
                logger.info(f"📦 캐시 히트! (key: {key})")
                return self._cache[key]
        return None
    
    def set(self, gender: str, calendar_type: str, birth_date: str, result: Any) -> None:
        """캐시에 운세 저장"""
        key = self._make_key(gender, calendar_type, birth_date)
        with self._lock:
            self._cache[key] = result
            logger.info(f"💾 캐시 저장 (key: {key})")
    
    def clear_old_entries(self) -> None:
        """오래된 캐시 항목 정리 (오늘 날짜가 아닌 항목 삭제)"""
        today = date.today().isoformat()
        with self._lock:
            keys_to_delete = [k for k in self._cache if not k.endswith(today)]
            for key in keys_to_delete:
                del self._cache[key]
            if keys_to_delete:
                logger.info(f"🧹 {len(keys_to_delete)}개의 오래된 캐시 삭제됨")


# ==================== 드라이버 풀링 시스템 ====================
class DriverPool:
    """
    Chrome 드라이버 풀링 시스템
    드라이버를 미리 생성해두고 재사용하여 시간 절약
    """
    def __init__(self, pool_size: int = 2, headless: bool = True):
        self._pool: Queue = Queue(maxsize=pool_size)
        self._pool_size = pool_size
        self._headless = headless
        self._lock = threading.Lock()
        self._initialized = False
        self._drivers_created = 0
    
    def _create_driver(self) -> webdriver.Chrome:
        """새 드라이버 생성"""
        options = webdriver.ChromeOptions()
        if self._headless:
            options.add_argument("--headless=new")
        options.add_argument("--disable-gpu")
        options.add_argument("--no-sandbox")
        options.add_argument("--disable-dev-shm-usage")
        options.add_argument("--window-size=1280,900")
        options.add_argument("--disable-extensions")
        options.add_argument("--disable-popup-blocking")
        options.add_argument("--disable-blink-features=AutomationControlled")
        options.add_experimental_option("excludeSwitches", ["enable-automation"])
        options.add_experimental_option('useAutomationExtension', False)
        options.add_argument("user-agent=Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
        options.set_capability('goog:loggingPrefs', {'browser': 'ALL'})
        
        service = Service(ChromeDriverManager().install())
        driver = webdriver.Chrome(service=service, options=options)
        self._drivers_created += 1
        logger.info(f"🚀 새 드라이버 생성 (총 {self._drivers_created}개)")
        return driver
    
    def initialize(self) -> None:
        """풀 초기화 (서버 시작 시 호출)"""
        with self._lock:
            if self._initialized:
                return
            logger.info(f"🏊 드라이버 풀 초기화 중... (크기: {self._pool_size})")
            for _ in range(self._pool_size):
                try:
                    driver = self._create_driver()
                    self._pool.put(driver)
                except Exception as e:
                    logger.error(f"❌ 드라이버 풀 초기화 실패: {e}")
            self._initialized = True
            logger.info(f"✅ 드라이버 풀 초기화 완료 ({self._pool.qsize()}개)")
    
    def get_driver(self, timeout: float = 30.0) -> webdriver.Chrome:
        """풀에서 드라이버 가져오기"""
        try:
            driver = self._pool.get(timeout=timeout)
            logger.debug(f"🔄 풀에서 드라이버 가져옴 (남은 수: {self._pool.qsize()})")
            return driver
        except Empty:
            # 풀이 비어있으면 새로 생성
            logger.warning("⚠️ 드라이버 풀이 비어있어 새로 생성합니다")
            return self._create_driver()
    
    def return_driver(self, driver: webdriver.Chrome) -> None:
        """드라이버를 풀에 반환"""
        try:
            # 브라우저 상태 초기화
            driver.delete_all_cookies()
            driver.get("about:blank")
            self._pool.put_nowait(driver)
            logger.debug(f"♻️ 드라이버 풀에 반환 (현재 수: {self._pool.qsize()})")
        except Exception as e:
            logger.warning(f"⚠️ 드라이버 반환 실패, 종료 후 새로 생성: {e}")
            try:
                driver.quit()
            except:
                pass
            # 새 드라이버로 교체
            try:
                new_driver = self._create_driver()
                self._pool.put_nowait(new_driver)
            except Exception as e2:
                logger.error(f"❌ 드라이버 교체 실패: {e2}")
    
    def shutdown(self) -> None:
        """모든 드라이버 종료"""
        logger.info("🛑 드라이버 풀 종료 중...")
        while not self._pool.empty():
            try:
                driver = self._pool.get_nowait()
                driver.quit()
            except:
                pass
        logger.info("✅ 드라이버 풀 종료 완료")


# 전역 캐시 및 드라이버 풀 인스턴스
fortune_cache = FortuneCache()
driver_pool: Optional[DriverPool] = None


def init_driver_pool(pool_size: int = 2, headless: bool = True) -> None:
    """드라이버 풀 초기화 (서버 시작 시 호출)"""
    global driver_pool
    if driver_pool is None:
        driver_pool = DriverPool(pool_size=pool_size, headless=headless)
        driver_pool.initialize()
        # 서버 종료 시 드라이버 풀 정리
        atexit.register(driver_pool.shutdown)


def cleanup_driver_pool() -> None:
    """드라이버 풀 정리 (서버 종료 시 호출)"""
    global driver_pool
    if driver_pool:
        driver_pool.shutdown()
        driver_pool = None

# 로깅 설정
logging.basicConfig(
    level=logging.DEBUG,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s',
    handlers=[
        logging.StreamHandler(),
        logging.FileHandler('naver_fortune_debug.log', encoding='utf-8')
    ]
)
logger = logging.getLogger(__name__)

# 디버그 파일 저장 경로
DEBUG_DIR = Path("debug_outputs")
DEBUG_DIR.mkdir(exist_ok=True)

BASE_URL = (
    "https://search.naver.com/search.naver?sm=tab_hty.top&where=nexearch&ssc=tab.nx.all&"
    "query=%EC%98%A4%EB%8A%98%EC%9D%98+%EC%9A%B4%EC%84%B8"
)

Gender = Literal["m", "f"]
CalendarType = Literal["solar", "lunarGeneral", "lunarLeap"]

SELECTOR_GENDER = (
    "#fortune_birthCondition > div.tb_box > div.system_select_box > div.gender._togglePanelSelect"
)
SELECTOR_CALENDAR = (
    "#fortune_birthCondition > div.tb_box > div.system_select_box > div.year._togglePanelSelect"
)
SELECTOR_DATE_TRIGGER = (
    "#fortune_birthCondition > div.tb_box > div.pop_select_box._dateCustomSelect > a > span > "
    "span.birth_date._trigger_text"
)
VISIBLE_DATE_POPUP = "div.select_box._root[aria-hidden='false']"
SELECTOR_SUBMIT = "#fortune_birthCondition > div.tb_box > button"
RESULT_PANEL = "#fortune_birthResult > div:nth-child(3) > dl:nth-child(3)"


class FortuneRequest(BaseModel):
    gender: Gender = Field(description='"m"=남성, "f"=여성')
    calendar_type: CalendarType = Field(
        description='"solar"=양력, "lunarGeneral"=음력 평달, "lunarLeap"=음력 윤달'
    )
    birth_date: str = Field(description="YYYY-MM-DD 형식의 생년월일")
    headless: bool = Field(default=True, description="브라우저 창 표시 여부")
    wait_seconds: int = Field(default=10, ge=1, le=30, description="요소 탐색 최대 대기 시간(초)")
    debug_mode: bool = Field(default=False, description="디버그 모드 활성화 (스크린샷/페이지소스 저장)")


@dataclass
class FortuneResult:
    title: str
    date_text: str
    summary: str


# ==================== 디버깅 헬퍼 함수들 ====================

def save_screenshot(driver: webdriver.Chrome, step_name: str) -> Optional[str]:
    """현재 브라우저 스크린샷을 저장합니다."""
    try:
        timestamp = datetime.now().strftime("%Y%m%d_%H%M%S")
        filename = DEBUG_DIR / f"screenshot_{step_name}_{timestamp}.png"
        driver.save_screenshot(str(filename))
        logger.info(f"📸 스크린샷 저장됨: {filename}")
        return str(filename)
    except Exception as e:
        logger.error(f"❌ 스크린샷 저장 실패: {e}")
        return None


def save_page_source(driver: webdriver.Chrome, step_name: str) -> Optional[str]:
    """현재 페이지 HTML 소스를 저장합니다."""
    try:
        timestamp = datetime.now().strftime("%Y%m%d_%H%M%S")
        filename = DEBUG_DIR / f"page_source_{step_name}_{timestamp}.html"
        with open(filename, 'w', encoding='utf-8') as f:
            f.write(driver.page_source)
        logger.info(f"📄 페이지 소스 저장됨: {filename}")
        return str(filename)
    except Exception as e:
        logger.error(f"❌ 페이지 소스 저장 실패: {e}")
        return None


def log_browser_state(driver: webdriver.Chrome, step_name: str) -> None:
    """현재 브라우저 상태를 로깅합니다."""
    try:
        logger.debug(f"🔍 [{step_name}] 브라우저 상태:")
        logger.debug(f"   - 현재 URL: {driver.current_url}")
        logger.debug(f"   - 페이지 제목: {driver.title}")
        logger.debug(f"   - 윈도우 핸들: {driver.current_window_handle}")
        
        # JavaScript 에러 확인
        logs = driver.get_log('browser')
        if logs:
            logger.debug(f"   - 브라우저 콘솔 로그:")
            for log in logs[-5:]:  # 최근 5개만
                logger.debug(f"     {log['level']}: {log['message'][:200]}")
    except Exception as e:
        logger.warning(f"브라우저 상태 로깅 실패: {e}")


def check_element_exists(driver: webdriver.Chrome, selector: str, by: By = By.CSS_SELECTOR) -> bool:
    """요소가 존재하는지 확인합니다."""
    try:
        elements = driver.find_elements(by, selector)
        exists = len(elements) > 0
        logger.debug(f"   요소 존재 확인 [{selector}]: {'✅ 존재' if exists else '❌ 없음'} (개수: {len(elements)})")
        return exists
    except Exception as e:
        logger.debug(f"   요소 확인 실패 [{selector}]: {e}")
        return False


def log_element_info(element, element_name: str) -> None:
    """요소의 상세 정보를 로깅합니다."""
    try:
        logger.debug(f"   📌 [{element_name}] 요소 정보:")
        logger.debug(f"      - 태그: {element.tag_name}")
        logger.debug(f"      - 텍스트: {element.text[:100] if element.text else '(없음)'}")
        logger.debug(f"      - 표시 여부: {element.is_displayed()}")
        logger.debug(f"      - 활성화 여부: {element.is_enabled()}")
        logger.debug(f"      - 위치: {element.location}")
        logger.debug(f"      - 크기: {element.size}")
    except Exception as e:
        logger.debug(f"   요소 정보 로깅 실패: {e}")


def save_debug_info_on_error(driver: webdriver.Chrome, step_name: str, error: Exception) -> dict:
    """에러 발생 시 디버그 정보를 저장하고 반환합니다."""
    debug_info = {
        "step": step_name,
        "error_type": type(error).__name__,
        "error_message": str(error),
        "traceback": traceback.format_exc(),
        "timestamp": datetime.now().isoformat(),
    }
    
    logger.error(f"🚨 [{step_name}] 에러 발생!")
    logger.error(f"   에러 타입: {debug_info['error_type']}")
    logger.error(f"   에러 메시지: {debug_info['error_message']}")
    logger.error(f"   트레이스백:\n{debug_info['traceback']}")
    
    try:
        debug_info["current_url"] = driver.current_url
        debug_info["page_title"] = driver.title
        debug_info["screenshot_path"] = save_screenshot(driver, f"error_{step_name}")
        debug_info["page_source_path"] = save_page_source(driver, f"error_{step_name}")
        log_browser_state(driver, f"error_{step_name}")
        
        # 주요 요소들 존재 여부 확인
        logger.debug("   주요 요소 존재 확인:")
        check_element_exists(driver, SELECTOR_GENDER)
        check_element_exists(driver, SELECTOR_CALENDAR)
        check_element_exists(driver, SELECTOR_DATE_TRIGGER)
        check_element_exists(driver, SELECTOR_SUBMIT)
        check_element_exists(driver, RESULT_PANEL)
    except Exception as e:
        logger.error(f"   디버그 정보 수집 중 추가 에러: {e}")
    
    return debug_info


# ==================== 메인 로직 함수들 ====================

def parse_birth_date(date_text: str) -> tuple[int, int, int]:
    logger.info(f"📅 생년월일 파싱 시작: {date_text}")
    try:
        parsed = datetime.strptime(date_text, "%Y-%m-%d").date()
        logger.debug(f"   파싱 결과: year={parsed.year}, month={parsed.month}, day={parsed.day}")
    except ValueError as exc:
        logger.error(f"❌ 생년월일 파싱 실패: {date_text}")
        raise ValueError("생년월일은 YYYY-MM-DD 형식이어야 합니다.") from exc

    if parsed.year < 1940:
        logger.error(f"❌ 지원하지 않는 연도: {parsed.year}")
        raise ValueError("네이버 위젯은 1940년 이후 생년월일만 지원합니다.")

    logger.info(f"✅ 생년월일 파싱 완료: {parsed.year}년 {parsed.month}월 {parsed.day}일")
    return parsed.year, parsed.month, parsed.day


def build_driver(*, headless: bool, enable_logging: bool = True) -> webdriver.Chrome:
    logger.info(f"🚀 Chrome 드라이버 빌드 시작 (headless={headless})")
    
    options = webdriver.ChromeOptions()
    if headless:
        options.add_argument("--headless=new")
        logger.debug("   headless 모드 활성화")
    options.add_argument("--disable-gpu")
    options.add_argument("--no-sandbox")
    options.add_argument("--disable-dev-shm-usage")
    options.add_argument("--window-size=1280,900")
    
    # 브라우저 로그 캡처 활성화
    if enable_logging:
        options.set_capability('goog:loggingPrefs', {'browser': 'ALL'})
        logger.debug("   브라우저 로깅 활성화")
    
    # 추가 안정성 옵션
    options.add_argument("--disable-extensions")
    options.add_argument("--disable-popup-blocking")
    options.add_argument("--disable-blink-features=AutomationControlled")
    options.add_experimental_option("excludeSwitches", ["enable-automation"])
    options.add_experimental_option('useAutomationExtension', False)
    
    # User-Agent 설정 (봇 탐지 우회)
    options.add_argument("user-agent=Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
    
    logger.debug("   Chrome 옵션 설정 완료")

    try:
        logger.debug("   ChromeDriverManager 설치 중...")
        service = Service(ChromeDriverManager().install())
        logger.debug("   ChromeDriver 설치 완료")
        
        driver = webdriver.Chrome(service=service, options=options)
        logger.info(f"✅ Chrome 드라이버 빌드 완료 (버전: {driver.capabilities.get('browserVersion', 'unknown')})")
        return driver
    except WebDriverException as e:
        logger.error(f"❌ Chrome 드라이버 빌드 실패: {e}")
        raise


def click_dropdown_option(
    driver: webdriver.Chrome,
    wait: WebDriverWait,
    container_selector: str,
    option_value: str,
    debug_mode: bool = False,
) -> None:
    logger.info(f"🔽 드롭다운 옵션 선택 시작: {option_value}")
    logger.debug(f"   컨테이너 셀렉터: {container_selector}")
    
    trigger_selector = f"{container_selector} > div > a"
    logger.debug(f"   트리거 셀렉터: {trigger_selector}")
    
    try:
        # 트리거 요소 대기 및 클릭
        logger.debug("   트리거 요소 대기 중...")
        check_element_exists(driver, trigger_selector)
        trigger = wait.until(EC.element_to_be_clickable((By.CSS_SELECTOR, trigger_selector)))
        log_element_info(trigger, "드롭다운 트리거")
        
        driver.execute_script("arguments[0].scrollIntoView({block: 'center'});", trigger)
        logger.debug("   트리거 요소로 스크롤 완료")
        
        if debug_mode:
            save_screenshot(driver, f"before_click_dropdown_{option_value}")
        
        trigger.click()
        logger.debug("   트리거 클릭 완료")
        
        # 옵션 선택
        option_selector = f"{container_selector} ul.group_list li[data-kgs-option='{option_value}']"
        logger.debug(f"   옵션 셀렉터: {option_selector}")
        
        logger.debug("   옵션 요소 대기 중...")
        check_element_exists(driver, option_selector)
        option = wait.until(EC.element_to_be_clickable((By.CSS_SELECTOR, option_selector)))
        log_element_info(option, f"옵션 {option_value}")
        
        driver.execute_script("arguments[0].scrollIntoView({block: 'center'});", option)
        option.click()
        logger.info(f"✅ 드롭다운 옵션 선택 완료: {option_value}")
        
    except TimeoutException as e:
        logger.error(f"❌ 드롭다운 옵션 선택 타임아웃: {option_value}")
        save_debug_info_on_error(driver, f"dropdown_{option_value}", e)
        raise
    except (NoSuchElementException, ElementClickInterceptedException) as e:
        logger.error(f"❌ 드롭다운 요소 찾기/클릭 실패: {e}")
        save_debug_info_on_error(driver, f"dropdown_{option_value}", e)
        raise


def pick_birth_date(
    driver: webdriver.Chrome,
    wait: WebDriverWait,
    year: int,
    month: int,
    day: int,
    debug_mode: bool = False,
) -> None:
    logger.info(f"📆 생년월일 선택 시작: {year}년 {month}월 {day}일")
    
    try:
        # 날짜 트리거 클릭
        logger.debug(f"   날짜 트리거 대기 중... (셀렉터: {SELECTOR_DATE_TRIGGER})")
        check_element_exists(driver, SELECTOR_DATE_TRIGGER)
        trigger = wait.until(EC.element_to_be_clickable((By.CSS_SELECTOR, SELECTOR_DATE_TRIGGER)))
        log_element_info(trigger, "날짜 트리거")
        
        driver.execute_script("arguments[0].scrollIntoView({block: 'center'});", trigger)
        
        if debug_mode:
            save_screenshot(driver, "before_date_trigger_click")
        
        trigger.click()
        logger.debug("   날짜 트리거 클릭 완료")
        
        # 팝업 대기
        logger.debug(f"   날짜 팝업 대기 중... (셀렉터: {VISIBLE_DATE_POPUP})")
        popup = wait.until(EC.visibility_of_element_located((By.CSS_SELECTOR, VISIBLE_DATE_POPUP)))
        logger.debug("   날짜 팝업 표시됨")
        
        if debug_mode:
            save_screenshot(driver, "date_popup_visible")

        # 그룹 요소 찾기
        groups = popup.find_elements(By.CSS_SELECTOR, "div.group_select._list_root")
        logger.debug(f"   날짜 선택 그룹 수: {len(groups)}")
        
        if len(groups) < 3:
            logger.error(f"❌ 날짜 선택 UI 그룹이 부족함: {len(groups)}개 (3개 필요)")
            save_debug_info_on_error(driver, "pick_birth_date_groups", RuntimeError("그룹 부족"))
            raise RuntimeError("날짜 선택 UI를 찾지 못했습니다.")

        # 연도, 월, 일 선택
        date_parts = [("연도", year), ("월", month), ("일", day)]
        for (part_name, value), group in zip(date_parts, groups[:3]):
            selector = f"li[data-value='{value}'] a"
            logger.debug(f"   {part_name} 선택 중: {value} (셀렉터: {selector})")
            
            try:
                option = group.find_element(By.CSS_SELECTOR, selector)
                log_element_info(option, f"{part_name} 옵션")
                driver.execute_script("arguments[0].scrollIntoView({block: 'center'});", option)
                option.click()
                logger.debug(f"   {part_name} {value} 선택 완료")
            except NoSuchElementException as e:
                logger.error(f"❌ {part_name} 옵션을 찾을 수 없음: {value}")
                # 사용 가능한 옵션들 로깅
                available_options = group.find_elements(By.CSS_SELECTOR, "li[data-value]")
                available_values = [opt.get_attribute("data-value") for opt in available_options[:20]]
                logger.error(f"   사용 가능한 {part_name} 값들: {available_values}")
                save_debug_info_on_error(driver, f"pick_{part_name}_{value}", e)
                raise

        # 팝업 닫기
        logger.debug("   날짜 팝업 닫기 (ESC 키)")
        driver.find_element(By.TAG_NAME, "body").send_keys(Keys.ESCAPE)
        
        try:
            wait.until(EC.invisibility_of_element_located((By.CSS_SELECTOR, VISIBLE_DATE_POPUP)))
            logger.debug("   날짜 팝업 정상 종료")
        except TimeoutException:
            logger.warning("   날짜 팝업이 자동으로 닫히지 않아 강제로 숨김 처리")
            driver.execute_script(
                "arguments[0].setAttribute('aria-hidden', 'true'); arguments[0].style.display='none';",
                popup,
            )
        
        logger.info(f"✅ 생년월일 선택 완료: {year}년 {month}월 {day}일")
        
    except TimeoutException as e:
        logger.error(f"❌ 생년월일 선택 타임아웃")
        save_debug_info_on_error(driver, "pick_birth_date_timeout", e)
        raise
    except Exception as e:
        logger.error(f"❌ 생년월일 선택 중 예상치 못한 에러: {type(e).__name__}: {e}")
        save_debug_info_on_error(driver, "pick_birth_date_error", e)
        raise


def click_button(driver: webdriver.Chrome, wait: WebDriverWait, selector: str, debug_mode: bool = False) -> None:
    logger.info(f"🔘 버튼 클릭 시작 (셀렉터: {selector})")
    
    try:
        check_element_exists(driver, selector)
        button = wait.until(EC.element_to_be_clickable((By.CSS_SELECTOR, selector)))
        log_element_info(button, "버튼")
        
        driver.execute_script("arguments[0].scrollIntoView({block: 'center'});", button)
        
        if debug_mode:
            save_screenshot(driver, "before_button_click")
        
        button.click()
        logger.info("✅ 버튼 클릭 완료")
        
    except TimeoutException as e:
        logger.error(f"❌ 버튼 클릭 타임아웃: {selector}")
        save_debug_info_on_error(driver, "click_button_timeout", e)
        raise
    except ElementClickInterceptedException as e:
        logger.error(f"❌ 버튼이 다른 요소에 가려져 있음: {selector}")
        save_debug_info_on_error(driver, "click_button_intercepted", e)
        raise


def extract_result(driver: webdriver.Chrome, wait: WebDriverWait, debug_mode: bool = False) -> FortuneResult:
    logger.info("📊 운세 결과 추출 시작")
    logger.debug(f"   결과 패널 셀렉터: {RESULT_PANEL}")
    
    try:
        check_element_exists(driver, RESULT_PANEL)
        panel = wait.until(EC.visibility_of_element_located((By.CSS_SELECTOR, RESULT_PANEL)))
        logger.debug("   결과 패널 발견")
        
        if debug_mode:
            save_screenshot(driver, "result_panel_visible")
        
        # 각 요소 추출
        try:
            title_elem = panel.find_element(By.CSS_SELECTOR, "strong")
            title = title_elem.text.strip()
            logger.debug(f"   제목 추출: {title}")
        except NoSuchElementException:
            logger.error("❌ 제목(strong) 요소를 찾을 수 없음")
            # 대체 셀렉터 시도
            logger.debug("   대체 셀렉터로 시도 중...")
            title = ""
            
        try:
            date_elem = panel.find_element(By.CSS_SELECTOR, "span.result_date")
            date_text = date_elem.text.strip()
            logger.debug(f"   날짜 추출: {date_text}")
        except NoSuchElementException:
            logger.error("❌ 날짜(span.result_date) 요소를 찾을 수 없음")
            date_text = ""
            
        try:
            summary_elem = panel.find_element(By.CSS_SELECTOR, "p")
            summary = summary_elem.text.strip()
            logger.debug(f"   요약 추출: {summary[:100]}...")
        except NoSuchElementException:
            logger.error("❌ 요약(p) 요소를 찾을 수 없음")
            summary = ""
        
        if not title or not summary:
            logger.warning("⚠️ 일부 결과 데이터가 비어있음")
            # 패널 전체 HTML 로깅
            logger.debug(f"   패널 HTML: {panel.get_attribute('innerHTML')[:500]}")
        
        result = FortuneResult(title=title, date_text=date_text, summary=summary)
        logger.info(f"✅ 운세 결과 추출 완료: {title}")
        return result
        
    except TimeoutException as e:
        logger.error("❌ 결과 패널 대기 타임아웃")
        save_debug_info_on_error(driver, "extract_result_timeout", e)
        raise
    except Exception as e:
        logger.error(f"❌ 결과 추출 중 예상치 못한 에러: {type(e).__name__}: {e}")
        save_debug_info_on_error(driver, "extract_result_error", e)
        raise


def fetch_today_fortune(
    gender: Gender,
    calendar_type: CalendarType,
    birth_date: str,
    *,
    headless: bool = True,
    wait_seconds: int = 10,
    debug_mode: bool = False,
    use_pool: bool = True,
) -> FortuneResult:
    """
    네이버 운세 크롤링 (최적화 버전)
    - 캐싱: 같은 날짜+생년월일 운세는 캐시에서 반환
    - 드라이버 풀링: 드라이버 재사용으로 시간 절약
    - sleep 제거: WebDriverWait으로 대체
    """
    
    # 1. 캐시 확인 (캐시 히트 시 즉시 반환)
    cached_result = fortune_cache.get(gender, calendar_type, birth_date)
    if cached_result:
        logger.info("⚡ 캐시에서 운세 반환 (크롤링 스킵)")
        return cached_result
    
    logger.info("=" * 60)
    logger.info("🌟 네이버 운세 크롤링 시작")
    logger.info(f"   성별: {gender}, 달력: {calendar_type}, 생년월일: {birth_date}")
    logger.info(f"   headless: {headless}, wait_seconds: {wait_seconds}, debug_mode: {debug_mode}")
    logger.info(f"   드라이버 풀 사용: {use_pool}")
    logger.info("=" * 60)
    
    start_time = datetime.now()
    
    year, month, day = parse_birth_date(birth_date)

    # 2. 드라이버 획득 (풀 사용 또는 새로 생성)
    driver = None
    using_pool = False
    
    if use_pool and driver_pool is not None:
        try:
            driver = driver_pool.get_driver(timeout=5.0)
            using_pool = True
            logger.info("🏊 드라이버 풀에서 드라이버 획득")
        except Exception as e:
            logger.warning(f"⚠️ 풀에서 드라이버 획득 실패, 새로 생성: {e}")
            driver = build_driver(headless=headless)
    else:
        driver = build_driver(headless=headless)
    
    wait = WebDriverWait(driver, wait_seconds)

    try:
        # Step 1: 페이지 로드
        logger.info("📍 Step 1: 페이지 로드")
        logger.debug(f"   URL: {BASE_URL}")
        driver.get(BASE_URL)
        
        # 페이지 로드 확인
        log_browser_state(driver, "page_load")
        if debug_mode:
            save_screenshot(driver, "step1_page_loaded")
            save_page_source(driver, "step1_page_loaded")
        
        # 페이지 로드 대기 (sleep 대신 명시적 대기 사용)
        wait.until(EC.presence_of_element_located((By.CSS_SELECTOR, SELECTOR_GENDER)))
        logger.debug("   페이지 로드 완료 (성별 선택기 발견)")
        
        # Step 2: 성별 선택
        logger.info("📍 Step 2: 성별 선택")
        click_dropdown_option(driver, wait, SELECTOR_GENDER, gender, debug_mode)
        if debug_mode:
            save_screenshot(driver, "step2_gender_selected")
        
        # Step 3: 달력 유형 선택
        logger.info("📍 Step 3: 달력 유형 선택")
        click_dropdown_option(driver, wait, SELECTOR_CALENDAR, calendar_type, debug_mode)
        if debug_mode:
            save_screenshot(driver, "step3_calendar_selected")
        
        # Step 4: 생년월일 선택
        logger.info("📍 Step 4: 생년월일 선택")
        pick_birth_date(driver, wait, year, month, day, debug_mode)
        if debug_mode:
            save_screenshot(driver, "step4_birthdate_selected")
        
        # Step 5: 제출 버튼 클릭
        logger.info("📍 Step 5: 운세 보기 버튼 클릭")
        click_button(driver, wait, SELECTOR_SUBMIT, debug_mode)
        if debug_mode:
            save_screenshot(driver, "step5_submit_clicked")
        
        # 결과 로드 대기 (sleep 대신 명시적 대기 사용)
        # extract_result 내부에서 WebDriverWait을 사용하므로 별도 대기 불필요
        
        # Step 6: 결과 추출
        logger.info("📍 Step 6: 결과 추출")
        result = extract_result(driver, wait, debug_mode)
        if debug_mode:
            save_screenshot(driver, "step6_result_extracted")
        
        elapsed_time = (datetime.now() - start_time).total_seconds()
        logger.info("=" * 60)
        logger.info(f"✅ 네이버 운세 크롤링 성공! (소요시간: {elapsed_time:.2f}초)")
        logger.info(f"   결과: {result.title} - {result.summary[:50]}...")
        logger.info("=" * 60)
        
        # 3. 결과 캐싱
        fortune_cache.set(gender, calendar_type, birth_date, result)
        
        return result
        
    except TimeoutException as exc:
        elapsed_time = (datetime.now() - start_time).total_seconds()
        logger.error("=" * 60)
        logger.error(f"❌ 네이버 운세 크롤링 실패 - 타임아웃 (소요시간: {elapsed_time:.2f}초)")
        save_debug_info_on_error(driver, "fetch_fortune_timeout", exc)
        logger.error("=" * 60)
        raise TimeoutException("페이지 요소를 제때 찾지 못했습니다. 네트워크 상태를 확인하세요.") from exc
        
    except (NoSuchElementException, StaleElementReferenceException) as exc:
        elapsed_time = (datetime.now() - start_time).total_seconds()
        logger.error("=" * 60)
        logger.error(f"❌ 네이버 운세 크롤링 실패 - 요소 찾기 실패 (소요시간: {elapsed_time:.2f}초)")
        save_debug_info_on_error(driver, "fetch_fortune_element_error", exc)
        logger.error("=" * 60)
        raise
        
    except Exception as exc:
        elapsed_time = (datetime.now() - start_time).total_seconds()
        logger.error("=" * 60)
        logger.error(f"❌ 네이버 운세 크롤링 실패 - 알 수 없는 에러 (소요시간: {elapsed_time:.2f}초)")
        logger.error(f"   에러 타입: {type(exc).__name__}")
        logger.error(f"   에러 메시지: {exc}")
        save_debug_info_on_error(driver, "fetch_fortune_unknown_error", exc)
        logger.error("=" * 60)
        raise
        
    finally:
        # 4. 드라이버 반환 또는 종료
        if using_pool and driver_pool is not None:
            logger.info("♻️ 드라이버를 풀에 반환")
            driver_pool.return_driver(driver)
        else:
            logger.info("🔚 Chrome 드라이버 종료")
            driver.quit()


app = FastAPI(title="Naver Fortune API", version="1.0.0")


@app.post("/fortune")
def get_fortune(payload: FortuneRequest):
    logger.info(f"🌐 API 요청 수신: {payload}")
    
    try:
        result = fetch_today_fortune(
            gender=payload.gender,
            calendar_type=payload.calendar_type,
            birth_date=payload.birth_date,
            headless=payload.headless,
            wait_seconds=payload.wait_seconds,
            debug_mode=payload.debug_mode,
        )
        
        response = {
            "date": result.date_text,
            "title": result.title,
            "summary": result.summary,
        }
        logger.info(f"✅ API 응답 성공: {response}")
        return response
        
    except (ValueError, RuntimeError, TimeoutException) as exc:
        logger.error(f"❌ API 에러 응답: {type(exc).__name__}: {exc}")
        raise HTTPException(status_code=400, detail=str(exc)) from exc
    except Exception as exc:
        logger.error(f"❌ API 예상치 못한 에러: {type(exc).__name__}: {exc}")
        logger.error(traceback.format_exc())
        raise HTTPException(status_code=500, detail=f"서버 내부 오류: {str(exc)}") from exc


if __name__ == "__main__":
    import uvicorn

    uvicorn.run("6_Naver_fortune_api:app", host="0.0.0.0", port=8000, reload=False)