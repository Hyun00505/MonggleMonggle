# AI API

꿈 해몽과 네이버 운세를 결합한 AI 기반 통합 운세 서비스입니다.

---

## ✨ 주요 기능

### 1. 통합 운세 조회

- **로컬 LLM 기반 꿈 해몽**: Gemma-2-9B 모델로 꿈 해석
- **네이버 운세 크롤링**: Selenium을 이용한 실시간 운세 정보 수집
- **AI 통합 분석**: Upstage Solar-Pro2 모델로 꿈과 운세를 종합 분석 \*추후 모델 바꿀수있음
- **행운의 색상 추천**: 당일 운세에 맞는 행운의 색상 추천

### 2. 🎨 꿈 이미지 생성

- **Gemini 2.0 Flash 이미지 생성**: 꿈 내용을 영화 같은 이미지로 변환
- **Base64 인코딩**: 즉시 사용 가능한 이미지 데이터 반환

---

## 🛠 기술 스택

### Backend

- **FastAPI**: 고성능 비동기 웹 프레임워크
- **Python 3.8+**: 메인 프로그래밍 언어

### AI/ML

- **llama-cpp-python**: 로컬 LLM 추론 (Gemma-2-9B)
- **Upstage Solar-Pro2**: 통합 분석 및 JSON 생성
- **Gemini 2.0 Flash**: 이미지 생성 (GMS API)

### Web Scraping

- **Selenium**: 네이버 운세 크롤링
- **webdriver-manager**: Chrome 드라이버 자동 관리

### Others

- **Pydantic**: 데이터 검증 및 직렬화
- **python-dotenv**: 환경 변수 관리
- **requests**: HTTP 요청 처리

---

## 📦 설치 및 실행

### 1. 저장소 클론

```bash
git clone https://github.com/your-repo/AI-api-service.git
cd AI-api-service
```

### 2. 가상환경 생성 및 활성화

```bash
# Windows
python -m venv venv
venv\Scripts\activate

# Linux/Mac
python3 -m venv venv
source venv/bin/activate
```

### 3. 의존성 설치

```bash
pip install -r requirements.txt
```

### 4. 모델 다운로드

`models/` 디렉토리에 다음 모델을 다운로드:

- `gemma-2-9b-it-dream-q4_k_m.gguf`

### 5. 서버 실행

```bash
# 개발 모드 (자동 재시작)
python main.py

# 또는
uvicorn main:app --reload --host 0.0.0.0 --port 8000
```

### 6. API 문서 확인

브라우저에서 다음 주소로 접속:

- Swagger UI: `http://localhost:8000/docs`
- ReDoc: `http://localhost:8000/redoc`

---

## 🔑 환경 변수 설정

프로젝트 루트에 `.env` 파일을 생성하고 다음 내용을 입력하세요:

```env
# Upstage API 키 (통합 운세 분석용)
UPSTAGE_API_KEY=your_upstage_api_key_here

# GMS API 키 (이미지 생성용)
GMS_API_KEY=your_gms_api_key_here
```

> **참고**: `.env` 파일이 없으면 코드에 하드코딩된 기본값이 사용됩니다.

---

# AI 통합 운세 서비스 API 명세서

## 1. 통합 운세 조회 API

사용자의 꿈 내용과 개인 정보를 기반으로 꿈 해몽, 오늘의 운세, 행운의 색상 및 아이템을 종합적으로 분석하여 제공합니다.

- **Endpoint:** `/api/v1/fortune/comprehensive`
- **Method:** `POST`
- **Content-Type:** `application/json`

### 요청 (Request)

| 필드명          | 타입   | 필수 | 설명                                                  | 예시                                            |
| :-------------- | :----- | :--: | :---------------------------------------------------- | :---------------------------------------------- |
| `name`          | string |  O   | 사용자 이름                                           | "김현빈"                                        |
| `dream_content` | string |  O   | 꿈 내용 상세                                          | "넓은 초원에서 유니콘을 타고 하늘을 나는 꿈..." |
| `gender`        | string |  O   | 성별 (`m`: 남성, `f`: 여성)                           | "m"                                             |
| `calendar_type` | string |  O   | 양력/음력 구분 (`solar`, `lunarGeneral`, `lunarLeap`) | "solar"                                         |
| `birth_date`    | string |  O   | 생년월일 (YYYY-MM-DD)                                 | "2000-05-05"                                    |

**Example Request:**

```json
{
  "name": "김현빈",
  "dream_content": "넓은 초원에서 유니콘을 타고 하늘을 나는 꿈을 꾸었습니다. 바람이 시원하게 느껴졌고 기분이 매우 상쾌했습니다.",
  "gender": "m",
  "calendar_type": "solar",
  "birth_date": "2000-05-05"
}
```

### 응답 (Response)

| 필드명                  | 타입   | 설명                                   |
| :---------------------- | :----- | :------------------------------------- |
| `title`                 | string | 운세 리포트 제목                       |
| `dream_interpretation`  | string | 1차 꿈 해몽 분석 결과 (LLM 생성)       |
| `today_fortune_summary` | string | 통합 운세 요약 (꿈 + 네이버 운세 종합) |
| `action_guide`          | string | 오늘의 행동 지침                       |
| `lucky_color`           | object | 행운의 색상 정보                       |
| `lucky_color.name`      | string | 색상 이름                              |
| `lucky_color.reason`    | string | 추천 이유                              |
| `lucky_item`            | object | 행운의 아이템 정보                     |
| `lucky_item.name`       | string | 아이템 이름                            |
| `lucky_item.reason`     | string | 추천 이유 (생년월일/성별 기반)         |

**Example Response:**

```json
{
  "title": "김현빈님의 2025-12-02 통합 운세",
  "dream_interpretation": "이 꿈은 자유와 높은 이상을 추구하는 심리를 나타냅니다...",
  "today_fortune_summary": "오늘은 예상치 못한 귀인을 만나 도움을 받을 수 있는 날입니다...",
  "action_guide": "새로운 제안이나 만남에 열린 태도를 유지하세요...",
  "lucky_color": {
    "name": "파란색",
    "reason": "꿈에서 느낀 시원한 바람과 하늘의 이미지에 가장 잘 맞는 색입니다."
  },
  "lucky_item": {
    "name": "은색 반지",
    "reason": "2000년생의 기운과 조화를 이루며 재물운을 상승시켜줍니다."
  }
}
```

---

## 2. 꿈 이미지 생성 API

꿈 내용을 바탕으로 AI가 시각화한 이미지를 생성합니다.

- **Endpoint:** `/api/v1/dream/generate-image`
- **Method:** `POST`
- **Content-Type:** `application/json`

### 요청 (Request)

| 필드명         | 타입   | 필수 | 설명                    | 예시                          | 기본값   |
| :------------- | :----- | :--: | :---------------------- | :---------------------------- | :------- |
| `dream_prompt` | string |  O   | 꿈 내용 (이미지 생성용) | "초원에서 유니콘을 타는 모습" | -        |
| `style`        | string |  X   | 이미지 스타일           | "수채화"                      | "몽환적" |

**지원되는 스타일 옵션:**

- `몽환적` (기본값): 신비롭고 몽환적인 분위기
- `수채화`: 부드러운 터치의 수채화 그림 스타일
- `애니메이션`: 2D 애니메이션/만화 작화 스타일
- `사실적`: 고화질 실사(Photo-realistic) 스타일
- `판타지`: 판타지 소설 표지 같은 웅장한 아트 스타일
- `추상적`: 현대 미술 같은 추상적인 스타일

**Example Request:**

```json
{
  "dream_prompt": "푸른 초원 위를 달리는 유니콘",
  "style": "애니메이션"
}
```

### 응답 (Response)

| 필드명                | 타입    | 설명                             |
| :-------------------- | :------ | :------------------------------- |
| `success`             | boolean | 성공 여부                        |
| `message`             | string  | 결과 메시지                      |
| `images`              | array   | 생성된 이미지 목록               |
| `images[].image_data` | string  | Base64 인코딩된 이미지 데이터    |
| `images[].mime_type`  | string  | 이미지 MIME 타입 (예: image/png) |
| `model_text`          | string  | (선택) 모델이 반환한 텍스트 설명 |

> **참고:** 생성된 이미지는 서버의 `./img` 디렉토리에도 파일로 자동 저장됩니다.

**Example Response:**

```json
{
  "success": true,
  "message": "1개의 이미지가 생성되었습니다.",
  "images": [
    {
      "image_data": "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mP8z8BQDwAEhQGAhKmMIQAAAABJRU5ErkJggg==",
      "mime_type": "image/png"
    }
  ],
  "model_text": null
}
```
