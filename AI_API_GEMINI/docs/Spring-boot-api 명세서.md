# 꿈 일기 & AI 해몽 서비스 API 명세서

## 프로젝트 개요

캘린더 기반 꿈 일기 작성 및 AI 해몽, 운세, 행운의 색 제공 서비스

### 기술 스택

- **Backend**: Java Spring Boot
- **AI Service**: FastAPI (Python)
- **Database**: MySQL

---

## 목차

1. [인증 API](#1-인증-api)
2. [꿈 일기 API](#2-꿈-일기-api)
3. [AI 분석 결과 API](#3-ai-분석-결과-api)
4. [감정 점수 API](#4-감정-점수-api)
5. [월별 분석 API](#5-월별-분석-api)
6. [월별 메모 API](#6-월별-메모-api)

---

## 1. 인증 API

### 1.1 회원가입

- **URL**: `/api/auth/signup`
- **Method**: `POST`
- **Description**: 새로운 사용자 회원가입
- **Request Body**:

```json
{
  "loginId": "string",
  "password": "string",
  "name": "string",
  "birthDate": "YYYY-MM-DD",
  "gender": "m | f",
  "calendarType": "solar | lunarGeneral | lunarLeap"
}
```

- **Response**:

```json
{
  "userId": 1,
  "loginId": "string",
  "name": "string",
  "gender": "M",
  "message": "회원가입이 완료되었습니다."
}
```

- **Status Codes**:
  - `201 Created`: 회원가입 성공
  - `400 Bad Request`: 잘못된 입력 데이터
  - `409 Conflict`: 이미 존재하는 아이디

### 1.2 로그인

- **URL**: `/api/auth/login`
- **Method**: `POST`
- **Description**: 사용자 로그인
- **Request Body**:

```json
{
  "loginId": "string",
  "password": "string"
}
```

- **Response**:

```json
{
  "userId": 1,
  "name": "string",
  "token": "jwt_token_here",
  "message": "로그인 성공"
}
```

- **Status Codes**:
  - `200 OK`: 로그인 성공
  - `401 Unauthorized`: 인증 실패
  - `404 Not Found`: 사용자를 찾을 수 없음

### 1.3 로그아웃

- **URL**: `/api/auth/logout`
- **Method**: `POST`
- **Description**: 사용자 로그아웃
- **Headers**: `Authorization: Bearer {token}`
- **Response**:

```json
{
  "message": "로그아웃 성공"
}
```

- **Status Codes**:
  - `200 OK`: 로그아웃 성공

### 1.4 사용자 정보 조회

- **URL**: `/api/auth/me`
- **Method**: `GET`
- **Description**: 현재 로그인한 사용자 정보 조회
- **Headers**: `Authorization: Bearer {token}`
- **Response**:

```json
{
  "userId": 1,
  "loginId": "string",
  "name": "string",
  "birthDate": "YYYY-MM-DD",
  "gender": "M",
  "calendarType": "solar",
  "createdDate": "YYYY-MM-DD HH:mm:ss"
}
```

- **Status Codes**:
  - `200 OK`: 조회 성공
  - `401 Unauthorized`: 인증 실패

### 1.5 사용자 정보 수정

- **URL**: `/api/auth/me`
- **Method**: `PUT`
- **Description**: 사용자 정보 수정
- **Headers**: `Authorization: Bearer {token}`
- **Request Body**:

```json
{
  "name": "string",
  "birthDate": "YYYY-MM-DD",
  "gender": "M | F",
  "password": "string (optional)"
}
```

- **Response**:

```json
{
  "message": "사용자 정보가 수정되었습니다."
}
```

- **Status Codes**:
  - `200 OK`: 수정 성공
  - `401 Unauthorized`: 인증 실패

### 1.6 회원 탈퇴

- **URL**: `/api/auth/me`
- **Method**: `DELETE`
- **Description**: 회원 탈퇴
- **Headers**: `Authorization: Bearer {token}`
- **Response**:

```json
{
  "message": "회원 탈퇴가 완료되었습니다."
}
```

- **Status Codes**:
  - `200 OK`: 탈퇴 성공
  - `401 Unauthorized`: 인증 실패

---

## 2. 꿈 일기 API

### 2.1 꿈 일기 작성

- **URL**: `/api/dreams`
- **Method**: `POST`
- **Description**: 새로운 꿈 일기 작성 및 AI 분석 요청
- **Headers**: `Authorization: Bearer {token}`
- **Request Body**:

```json
{
  "emotionId": 1,
  "dreamDate": "YYYY-MM-DD",
  "title": "string",
  "content": "string"
}
```

- **Response**:

```json
{
  "dreamId": 1,
  "title": "string",
  "content": "string",
  "dreamDate": "YYYY-MM-DD",
  "emotionId": 1,
  "createdDate": "YYYY-MM-DD HH:mm:ss",
  "message": "꿈 일기가 저장되고 AI 분석이 요청되었습니다."
}
```

- **Status Codes**:
  - `201 Created`: 작성 성공
  - `400 Bad Request`: 잘못된 입력 데이터
  - `401 Unauthorized`: 인증 실패

### 2.2 꿈 일기 목록 조회 (월별)

- **URL**: `/api/dreams`
- **Method**: `GET`
- **Description**: 특정 월의 꿈 일기 목록 조회
- **Headers**: `Authorization: Bearer {token}`
- **Query Parameters**:
  - `year`: 연도 (required)
  - `month`: 월 (required)
- **Response**:

```json
{
  "year": 2025,
  "month": 11,
  "dreams": [
    {
      "dreamId": 1,
      "title": "string",
      "dreamDate": "YYYY-MM-DD",
      "emotionId": 1,
      "emotionName": "string",
      "hasResult": true
    }
  ]
}
```

- **Status Codes**:
  - `200 OK`: 조회 성공
  - `401 Unauthorized`: 인증 실패

### 2.3 꿈 일기 상세 조회

- **URL**: `/api/dreams/{dreamId}`
- **Method**: `GET`
- **Description**: 특정 꿈 일기 상세 조회
- **Headers**: `Authorization: Bearer {token}`
- **Path Parameters**:
  - `dreamId`: 꿈 일기 ID
- **Response**:

```json
{
  "dreamId": 1,
  "userId": 1,
  "emotionId": 1,
  "emotionName": "string",
  "dreamDate": "YYYY-MM-DD",
  "title": "string",
  "content": "string",
  "createdDate": "YYYY-MM-DD HH:mm:ss",
  "deletedDate": null
}
```

- **Status Codes**:
  - `200 OK`: 조회 성공
  - `401 Unauthorized`: 인증 실패
  - `403 Forbidden`: 권한 없음
  - `404 Not Found`: 일기를 찾을 수 없음

### 2.4 꿈 일기 수정

- **URL**: `/api/dreams/{dreamId}`
- **Method**: `PUT`
- **Description**: 꿈 일기 수정
- **Headers**: `Authorization: Bearer {token}`
- **Path Parameters**:
  - `dreamId`: 꿈 일기 ID
- **Request Body**:

```json
{
  "emotionId": 1,
  "title": "string",
  "content": "string"
}
```

- **Response**:

```json
{
  "dreamId": 1,
  "message": "꿈 일기가 수정되었습니다."
}
```

- **Status Codes**:
  - `200 OK`: 수정 성공
  - `400 Bad Request`: 잘못된 입력 데이터
  - `401 Unauthorized`: 인증 실패
  - `403 Forbidden`: 권한 없음
  - `404 Not Found`: 일기를 찾을 수 없음

### 2.5 꿈 일기 삭제

- **URL**: `/api/dreams/{dreamId}`
- **Method**: `DELETE`
- **Description**: 꿈 일기 삭제 (Soft Delete)
- **Headers**: `Authorization: Bearer {token}`
- **Path Parameters**:
  - `dreamId`: 꿈 일기 ID
- **Response**:

```json
{
  "message": "꿈 일기가 삭제되었습니다."
}
```

- **Status Codes**:
  - `200 OK`: 삭제 성공
  - `401 Unauthorized`: 인증 실패
  - `403 Forbidden`: 권한 없음
  - `404 Not Found`: 일기를 찾을 수 없음

## 3. AI 분석 결과 API

### 3.1 AI 분석 결과 조회

- **URL**: `/api/dreams/{dreamId}/result`
- **Method**: `GET`
- **Description**: 특정 꿈 일기의 AI 분석 결과 조회
- **Headers**: `Authorization: Bearer {token}`
- **Path Parameters**:
  - `dreamId`: 꿈 일기 ID
- **Response**:

```json
{
  "id": 1,
  "dreamId": 1,
  "fortuneId": 1,
  "dreamInterpretation": "AI 해몽 결과 텍스트",
  "todayFortuneSummary": "오늘의 운세 종합 텍스트",
  "actionGuide": "오늘의 행동 지침",
  "luckyColor": {
    "name": "파란색",
    "reason": "색상 추천 이유"
  },
  "luckyItem": {
    "name": "은색 반지",
    "reason": "아이템 추천 이유"
  },
  "imageUrl": "string",
  "createdDate": "YYYY-MM-DD HH:mm:ss",
  "deletedDate": null
}
```

- **Status Codes**:
  - `200 OK`: 조회 성공
  - `401 Unauthorized`: 인증 실패
  - `403 Forbidden`: 권한 없음
  - `404 Not Found`: 분석 결과를 찾을 수 없음

### 3.2 AI 분석 재요청

- **URL**: `/api/dreams/{dreamId}/analyze`
- **Method**: `POST`
- **Description**: AI 분석 재요청 (FastAPI 호출)
- **Headers**: `Authorization: Bearer {token}`
- **Path Parameters**:
  - `dreamId`: 꿈 일기 ID
- **Response**:

```json
{
  "message": "AI 분석이 재요청되었습니다.",
  "dreamId": 1
}
```

- **Status Codes**:
  - `200 OK`: 재요청 성공
  - `401 Unauthorized`: 인증 실패
  - `403 Forbidden`: 권한 없음
  - `404 Not Found`: 일기를 찾을 수 없음
  - `503 Service Unavailable`: AI 서비스 오류

---

## 4. 감정 점수 API

### 4.1 감정 점수 목록 조회 (감정 점수 매핑 더 생각해봐야댐 지민이가 3단계라고 그랬자나 근데 내 생각엔 5단계가 더 좋아보여 구체적이잖아.)

- **URL**: `/api/emotions`
- **Method**: `GET`
- **Description**: 사용 가능한 감정 점수 목록 조회
- **Headers**: `Authorization: Bearer {token}`
- **Response**:

```json
{
  "emotions": [
    {
      "emotionId": 1,
      "emotionName": "기쁨",
      "score": 5
    },
    {
      "emotionId": 2,
      "emotionName": "슬픔",
      "score": 1
    }
  ]
}
```

- **Status Codes**:
  - `200 OK`: 조회 성공
  - `401 Unauthorized`: 인증 실패

---

## 5. 월별 분석 API

### 5.1 월별 꿈 분석 조회 (월별 분석 기능 개발중이야! 조금만 기다려줘 일단 이거 읽어보기만해)

- **URL**: `/api/analysis/monthly`
- **Method**: `GET`
- **Description**: 특정 월의 꿈 통계 및 분석 결과 조회
- **Headers**: `Authorization: Bearer {token}`
- **Query Parameters**:
  - `year`: 연도 (required)
  - `month`: 월 (required)
- **Response**:

```json
{
  "analysisId": 1,
  "year": 2025,
  "month": 11,
  "dreamCount": 15,
  "avgEmotionScore": 3.5,
  "createdDate": "YYYY-MM-DD HH:mm:ss",
  "updatedDate": "YYYY-MM-DD HH:mm:ss"
}
```

- **Status Codes**:
  - `200 OK`: 조회 성공
  - `401 Unauthorized`: 인증 실패
  - `404 Not Found`: 분석 데이터 없음

### 5.2 월별 분석 데이터 생성/갱신

- **URL**: `/api/analysis/monthly`
- **Method**: `POST`
- **Description**: 특정 월의 분석 데이터 생성 또는 갱신
- **Headers**: `Authorization: Bearer {token}`
- **Request Body**:

```json
{
  "year": 2025,
  "month": 11
}
```

- **Response**:

```json
{
  "analysisId": 1,
  "dreamCount": 15,
  "avgEmotionScore": 3.5,
  "message": "월별 분석이 완료되었습니다."
}
```

- **Status Codes**:
  - `200 OK`: 갱신 성공
  - `201 Created`: 생성 성공
  - `401 Unauthorized`: 인증 실패

---

## 6. 월별 메모 API

### 6.1 월별 메모 조회

- **URL**: `/api/memo/monthly`
- **Method**: `GET`
- **Description**: 특정 월의 메모 조회
- **Headers**: `Authorization: Bearer {token}`
- **Query Parameters**:
  - `year`: 연도 (required)
  - `month`: 월 (required)
- **Response**:

```json
{
  "memoId": 1,
  "analysisId": 1,
  "year": 2025,
  "month": 11,
  "memoContent": "이번 달 꿈 일기 메모 내용",
  "createdDate": "YYYY-MM-DD HH:mm:ss",
  "updatedDate": "YYYY-MM-DD HH:mm:ss"
}
```

- **Status Codes**:
  - `200 OK`: 조회 성공
  - `401 Unauthorized`: 인증 실패
  - `404 Not Found`: 메모 없음

### 6.2 월별 메모 작성/수정

- **URL**: `/api/memo/monthly`
- **Method**: `POST`
- **Description**: 특정 월의 메모 작성 또는 수정
- **Headers**: `Authorization: Bearer {token}`
- **Request Body**:

```json
{
  "analysisId": 1,
  "memoContent": "메모 내용"
}
```

- **Response**:

```json
{
  "memoId": 1,
  "message": "메모가 저장되었습니다."
}
```

- **Status Codes**:
  - `200 OK`: 수정 성공
  - `201 Created`: 작성 성공
  - `401 Unauthorized`: 인증 실패

### 6.3 월별 메모 삭제

- **URL**: `/api/memo/monthly/{memoId}`
- **Method**: `DELETE`
- **Description**: 월별 메모 삭제
- **Headers**: `Authorization: Bearer {token}`
- **Path Parameters**:
  - `memoId`: 메모 ID
- **Response**:

```json
{
  "message": "메모가 삭제되었습니다."
}
```

- **Status Codes**:
  - `200 OK`: 삭제 성공
  - `401 Unauthorized`: 인증 실패
  - `403 Forbidden`: 권한 없음
  - `404 Not Found`: 메모를 찾을 수 없음

---

## 공통 사항

### 인증 방식 (인증 방식은 JWT로 해뒀어 다른 토큰이나 방식 생각해둔거 있으면 그거 써도 좋아.)

- JWT (JSON Web Token) 기반 인증
- Header: `Authorization: Bearer {token}`

### 에러 응답 형식

```json
{
  "error": "error_code",
  "message": "에러 메시지",
  "timestamp": "YYYY-MM-DD HH:mm:ss"
}
```

### 공통 Status Codes

- `401 Unauthorized`: 인증 토큰이 없거나 유효하지 않음
- `403 Forbidden`: 접근 권한 없음
- `500 Internal Server Error`: 서버 오류

---

## FastAPI 연동 명세서

Python FastAPI로 구현된 AI 서비스 엔드포인트입니다.

### API 1: 통합 운세 조회

- **URL**: `{FASTAPI_URL}/api/v1/fortune/comprehensive`
- **Method**: `POST`
- **Description**: 꿈 해몽, 네이버 운세, 종합 분석, 행운의 색 제공
- **Process**:

  1. 로컬 LLM으로 꿈 해몽
  2. 네이버 운세 크롤링 (생년월일 기반)
  3. Upstage API로 종합 분석 및 행운의 색상 추천

- **Request Body**:

```json
{
  "name": "string",
  "dream_content": "string",
  "gender": "m | f",
  "calendar_type": "solar | lunarGeneral | lunarLeap",
  "birth_date": "YYYY-MM-DD"
}
```

**필드 설명:**

- `name`: 사용자 이름
- `dream_content`: 꿈 내용 텍스트
- `gender`: 성별 (`m`: 남성, `f`: 여성)
- `calendar_type`: 생년월일 유형
  - `solar`: 양력
  - `lunarGeneral`: 음력 (평달)
  - `lunarLeap`: 음력 (윤달)
- `birth_date`: 생년월일 (YYYY-MM-DD 형식)

- **Response (200 OK)**:

```json
{
  "title": "OOO님의 YYYY-MM-DD 통합 운세",
  "dream_interpretation": "꿈 해몽 결과 텍스트",
  "today_fortune_summary": "오늘의 운세 종합 요약",
  "action_guide": "실천 가이드 (행동 지침)",
  "lucky_color": {
    "name": "색상 이름",
    "reason": "해당 색상을 추천하는 이유"
  },
  "lucky_item": {
    "name": "아이템 이름",
    "reason": "해당 아이템을 추천하는 이유"
  }
}
```

**필드 설명:**

- `title`: 운세 제목
- `dream_interpretation`: 로컬 LLM의 꿈 해몽 결과
- `today_fortune_summary`: 네이버 운세 + 꿈 해몽을 종합한 오늘의 운세
- `action_guide`: 오늘 하루 실천할 수 있는 행동 지침
- `lucky_color`: 오늘의 행운의 색

  - `name`: 색상 이름 (한글)
  - `reason`: 추천 이유

- `lucky_item`: 오늘의 행운의 아이템

  - `name`: 아이템 이름
  - `reason`: 추천 이유 (생년월일/성별 기반)

- **Status Codes**:
  - `200 OK`: 정상 응답
  - `422 Unprocessable Entity`: 유효성 검증 실패

---

### API 2: 꿈 이미지 생성

- **URL**: `{FASTAPI_URL}/api/v1/dream/generate-image`
- **Method**: `POST`
- **Description**: 꿈 내용을 기반으로 AI 이미지 생성
- **Model**: Gemini 2.0 Flash Exp Image Generation

- **Request Body**:

```json
{
  "dream_prompt": "string",
  "style": "string (optional)"
}
```

**필드 설명:**

- `dream_prompt`: 꿈 내용 또는 이미지 생성 프롬프트
- `style`: 이미지 스타일 (선택사항, 기본값: "몽환적")

**지원되는 스타일 옵션:**

- `몽환적` (기본값): 신비롭고 몽환적인 분위기
- `수채화`: 부드러운 터치의 수채화 그림 스타일
- `애니메이션`: 2D 애니메이션/만화 작화 스타일
- `사실적`: 고화질 실사(Photo-realistic) 스타일
- `판타지`: 판타지 소설 표지 같은 웅장한 아트 스타일
- `추상적`: 현대 미술 같은 추상적인 스타일

- **Response (200 OK)**:

```json
{
  "success": true,
  "message": "N개의 이미지가 생성되었습니다.",
  "images": [
    {
      "image_data": "base64_encoded_image_data",
      "mime_type": "image/png"
    }
  ],
  "model_text": "모델 응답 텍스트 (optional)"
}
```

**필드 설명:**

- `success`: 생성 성공 여부
- `message`: 응답 메시지
- `images`: 생성된 이미지 배열
  - `image_data`: Base64로 인코딩된 이미지 데이터
  - `mime_type`: 이미지 MIME 타입 (image/png)
- `model_text`: AI 모델의 추가 응답 텍스트 (있을 경우)

> **참고:** 생성된 이미지는 서버의 `./img` 디렉토리에도 파일로 자동 저장됩니다.

- **Status Codes**:
  - `200 OK`: 정상 응답
  - `422 Unprocessable Entity`: 유효성 검증 실패

---

### Spring Boot에서 FastAPI 호출 시나리오

#### 시나리오 1: 꿈 일기 작성 시 자동 분석

1. 사용자가 꿈 일기 작성 (`POST /api/dreams`)
2. Spring Boot에서 꿈 일기 저장
3. FastAPI `/api/v1/fortune/comprehensive` 호출 (비동기 권장)
   - 사용자 정보(이름, 생년월일, 성별)와 꿈 내용 전달
4. FastAPI 응답을 `dream_results` 테이블에 저장
   - `dream_interpretation`: 꿈 해몽
   - `today_fortune_summary`: 오늘의 운세
   - `action_guide`: 행동 지침
   - `lucky_color_name`, `lucky_color_reason`: 행운의 색
   - `lucky_item_name`, `lucky_item_reason`: 행운의 아이템
5. 사용자에게 완료 응답

#### 시나리오 2: 이미지 생성 요청

1. 사용자가 특정 꿈 일기에 대한 이미지 생성 요청 (선택적으로 style 파라미터 포함)
2. Spring Boot에서 FastAPI `/api/v1/dream/generate-image` 호출
   - `dream_prompt`: 꿈 내용
   - `style` (선택사항): 몽환적, 수채화, 애니메이션, 사실적, 판타지, 추상적
3. Base64 이미지 데이터를 받아 파일로 저장 (S3 등)
4. 저장된 이미지 URL을 `dream_results.image_url`에 업데이트

#### 에러 처리

- FastAPI 서비스가 응답하지 않는 경우: `503 Service Unavailable` 반환
- FastAPI에서 422 에러 발생 시: 사용자에게 입력 데이터 검증 오류 메시지 전달
- 타임아웃 설정 권장: 30-60초 (AI 처리 시간 고려)

---

### FastAPI 환경 설정

**Spring Boot application.properties 예시:**

```properties
# FastAPI 서비스 URL
fastapi.url=http://localhost:8000
fastapi.timeout=60000

# FastAPI 연동 비동기 처리 설정
fastapi.async.enabled=true
```

**FastAPI 서버 정보:**

- Base URL: `http://localhost:8000` (개발 환경)
- API Version: v1
- Framework: FastAPI 1.0.0
- Documentation: `/docs` (Swagger UI)
- OpenAPI Schema: `/openapi.json`

---

## 데이터베이스 테이블 구조

### users (회원 정보)

- user_id (BIGINT, PK, NOT NULL)
- name (VARCHAR(100), NOT NULL)
- birth_date (DATE, NOT NULL)
- gender (CHAR(1), NOT NULL) - 'M': 남성, 'F': 여성
- calendar_type (VARCHAR(20), NOT NULL) - 'solar': 양력, 'lunarGeneral': 음력(평달), 'lunarLeap': 음력(윤달)
- login_id (VARCHAR(255), NOT NULL)
- password (VARCHAR(255), NOT NULL)
- created_date (DATETIME, NOT NULL)
- updated_date (DATETIME, NULL)
- deleted_date (DATETIME, NULL)

### dreams (꿈 기록)

- dream_id (BIGINT, PK, NOT NULL)
- user_id (BIGINT, FK, NOT NULL) → users(user_id)
- emotion_id (TINYINT, FK, NOT NULL) → emotion_scores(emotion_id)
- dream_date (DATE, NOT NULL)
- title (TEXT, NOT NULL)
- content (TEXT, NOT NULL)
- created_date (DATETIME, NOT NULL)
- deleted_date (DATETIME, NULL)

### dream_results (AI 분석 결과)

- id (BIGINT, PK, NOT NULL)
- dream_id (BIGINT, FK, NOT NULL) → dreams(dream_id)
- fortune_id (BIGINT, FK, NOT NULL)
- dream_interpretation (TEXT, NOT NULL) - 꿈 해몽 결과
- today_fortune_summary (TEXT, NOT NULL) - 오늘의 운세 종합
- action_guide (TEXT, NOT NULL) - 행동 지침
- lucky_color_name (VARCHAR(50), NOT NULL) - 행운의 색 이름
- lucky_color_reason (TEXT, NOT NULL) - 행운의 색 추천 이유
- lucky_item_name (VARCHAR(100), NOT NULL) - 행운의 아이템 이름
- lucky_item_reason (TEXT, NOT NULL) - 행운의 아이템 추천 이유
- image_url (VARCHAR(255), NULL) - 꿈 이미지 URL
- created_date (DATETIME, NOT NULL)
- deleted_date (DATETIME, NULL)

### emotion_scores (감정 점수)

- emotion_id (TINYINT, PK, NOT NULL)
- emotion_name (VARCHAR(20), NOT NULL)
- score (INT, NOT NULL)

### dream_monthly_analysis (월별 꿈 분석)

- analysis_id (BIGINT, PK, NOT NULL)
- user_id (BIGINT, FK, NOT NULL) → users(user_id)
- year (INT, NOT NULL)
- month (INT, NOT NULL)
- dream_count (INT, NOT NULL)
- avg_emotion_score (DECIMAL(5,2), NOT NULL)
- created_date (DATETIME, NOT NULL)
- updated_date (DATETIME, NULL)

### dream_monthly_memo (월별 메모)

- memo_id (BIGINT, PK, NOT NULL)
- analysis_id (BIGINT, FK, NOT NULL) → dream_monthly_analysis(analysis_id)
- memo_content (TEXT, NOT NULL)
- created_date (DATETIME, NOT NULL)
- updated_date (DATETIME, NULL)
- deleted_date (DATETIME, NULL)

---

## 개발 환경 설정

### application.properties 예시

```properties
# Database
spring.datasource.url=jdbc:mysql://localhost:3306/dream_db
spring.datasource.username=your_username
spring.datasource.password=your_password

# JPA
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true

# JWT
jwt.secret=your_secret_key
jwt.expiration=86400000

# FastAPI
fastapi.url=http://localhost:8000
```

---

## 참고 사항

1. 모든 날짜는 `YYYY-MM-DD` 형식 사용
2. 모든 날짜/시간은 `YYYY-MM-DD HH:mm:ss` 형식 사용
3. 삭제는 Soft Delete 방식 사용 (deleted_date 컬럼 활용)
4. FastAPI와의 통신은 비동기 처리 권장
5. 이미지 URL은 외부 저장소(S3 등) 사용 권장
