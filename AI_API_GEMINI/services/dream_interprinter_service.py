import os
from pathlib import Path
from typing import Optional, List, Union, Dict
from dotenv import load_dotenv
from google import genai

# .env 파일에서 환경변수 로드 (현재 파일 기준 상위 폴더의 .env)
ENV_PATH = Path(__file__).resolve().parent.parent / ".env"
load_dotenv(ENV_PATH, override=True)

# Gemini API 키 설정 (환경변수에서만 참조)
GEMINI_API_KEY = os.getenv("GEMINI_API_KEY")

# Gemini 클라이언트 초기화
gemini_client = genai.Client(api_key=GEMINI_API_KEY)

# 전역 모델 저장소 (하위 호환성을 위해 유지, 실제로는 사용하지 않음)
models = {"llm": "gemini-2.5-flash"}


def generate_response(
    llm: Optional[str] = None,
    user_prompt: Union[str, List[Dict[str, str]]] = "",
    system_prompt: str = "",
    history: Union[None, List[Dict[str, str]]] = None,
    max_tokens: int = 1024,
    temperature: float = 0.7,
    top_p: float = 0.9,
    repeat_penalty: float = 1.05,
) -> str:
    """
    Gemini API를 사용하여 응답 생성
    
    Args:
        llm: 사용하지 않음 (하위 호환성을 위해 유지)
        user_prompt: 사용자 프롬프트 (문자열 또는 메시지 리스트)
        system_prompt: 시스템 프롬프트
        history: 대화 히스토리
        max_tokens: 최대 토큰 수 (참고용, Gemini에서는 자동 관리)
        temperature: 온도 파라미터
        top_p: top_p 파라미터 (참고용)
        repeat_penalty: 반복 페널티 (참고용)
        
    Returns:
        str: 생성된 응답 텍스트
    """
    
    # 프롬프트 구성
    if isinstance(user_prompt, list):
        # 메시지 리스트인 경우 텍스트로 변환
        prompt_parts = []
        for msg in user_prompt:
            role = msg.get("role", "user")
            content = msg.get("content", "")
            if role == "system":
                prompt_parts.insert(0, f"[시스템 지시]\n{content}\n")
            elif role == "user":
                prompt_parts.append(f"[사용자]\n{content}\n")
            elif role == "assistant":
                prompt_parts.append(f"[어시스턴트]\n{content}\n")
        full_prompt = "\n".join(prompt_parts)
    else:
        full_prompt = user_prompt
    
    # 시스템 프롬프트 추가
    if system_prompt:
        full_prompt = f"[시스템 지시]\n{system_prompt}\n\n{full_prompt}"
    
    # 히스토리 추가
    if history:
        history_text = ""
        for msg in history:
            role = msg.get("role", "user")
            content = msg.get("content", "")
            if role == "user":
                history_text += f"[사용자]\n{content}\n\n"
            elif role == "assistant":
                history_text += f"[어시스턴트]\n{content}\n\n"
        full_prompt = f"{history_text}{full_prompt}"
    
    try:
        # Gemini API 호출
        response = gemini_client.models.generate_content(
            model="gemini-2.5-flash",
            contents=full_prompt
        )
        
        return response.text.strip() if response.text else ""
        
    except Exception as e:
        print(f"❌ Gemini API 응답 생성 실패: {e}")
        raise RuntimeError(f"Gemini API 응답 생성 중 오류 발생: {e}")


# 하위 호환성을 위한 더미 함수들 (실제로는 사용하지 않음)
def load_llama_model(
    model_key: str = "q4_k_m",
    n_ctx: int = 4096,
    n_batch: int = 512,
    n_gpu_layers: int = 0,
    seed: Optional[int] = 42,
    verbose: bool = False,
) -> str:
    """
    더미 함수 - Gemini API 사용으로 인해 실제 모델 로드 필요 없음
    하위 호환성을 위해 문자열 반환
    """
    print("✅ Gemini API 사용 - 로컬 모델 로드 생략")
    return "gemini-2.5-flash"


def build_messages(
    user_prompt: Union[str, List[Dict[str, str]]],
    system_prompt: str = "",
    history: Union[None, List[Dict[str, str]]] = None,
    include_system: bool = True,
) -> List[Dict[str, str]]:
    """메시지 배열 구성 (하위 호환성을 위해 유지)"""
    messages: List[Dict[str, str]] = []
    if include_system and system_prompt:
        messages.append({"role": "system", "content": system_prompt})
    if history:
        messages.extend(history)
    if isinstance(user_prompt, str):
        messages.append({"role": "user", "content": user_prompt})
    else:
        messages.extend(user_prompt)
    return messages
