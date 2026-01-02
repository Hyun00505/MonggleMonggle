# 꿈 일기 & AI 해몽 서비스 API 명세서

## 프로젝트 개요

캘린더 기반 꿈 일기 작성 및 AI 해몽, 운세, 행운의 색 제공 서비스

### 기술 스택

- **Backend**: Java Spring Boot
- **AI Service**: FastAPI (Python)
- **Database**: MySQL

---

## 시스템 아키텍처

```
                ┌─────────────┐
                │  Frontend   │
                └──────┬──────┘
                       │
        ┌──────────────┼──────────────┐
        │              │              │
        ▼              ▼              ▼
  [일일 운세]     [이미지 생성]  [데이터 CRUD]
        │              │              │
        │              │              │
        ▼              ▼              ▼
┌─────────────┐   ┌─────────────┐   ┌─────────────┐
│   FastAPI   │   │   FastAPI   │   │ Spring Boot │
│  (AI 직접)  │   │  (AI 직접)  │   │  (백엔드)   │
└─────────────┘   └─────────────┘   └──────┬──────┘
                                            │
                                            ▼
                                     ┌─────────────┐
                                     │    MySQL    │
                                     │(데이터베이스)│
                                     └─────────────┘
                                            ▲
                                            │
[월간 분석]                                 │
프론트 → Spring Boot ──→ FastAPI            │
         (데이터 조회)     (AI 분석)        │
              │ ←─────────── ┘              │
              └─────────────────────────────┘
                   (결과 저장)
```

### 통신 구조

1. **프론트 → Spring Boot (데이터 관리)**

   - 회원 인증 (로그인/회원가입)
   - 꿈 일기 CRUD
   - AI 분석 결과 저장 및 조회
   - **월별 분석 요청 및 조회** ⭐
   - 월별 메모 CRUD

2. **프론트 → FastAPI (AI 직접 통신)**

   - AI 꿈 해몽 및 운세 분석 (일일)
   - 꿈 이미지 생성

3. **Spring Boot → FastAPI (서버 간 통신)**
   - 월간 리포트 생성 요청 ⭐

---

## 목차

### Spring Boot API (데이터 관리)

1. [인증 API](#1-인증-api-spring-boot)
2. [꿈 일기 API](#2-꿈-일기-api-spring-boot)
3. [AI 분석 결과 저장 API](#3-ai-분석-결과-저장-api-spring-boot)
4. [감정 점수 API](#4-감정-점수-api-spring-boot)
5. [월별 분석 API](#5-월별-분석-api-spring-boot)
6. [월별 메모 API](#6-월별-메모-api-spring-boot)

### FastAPI (AI 서비스)

7. [AI 통합 운세 API](#7-ai-통합-운세-api-fastapi)
8. [AI 이미지 생성 API](#8-ai-이미지-생성-api-fastapi)
9. [AI 월간 분석 API](#9-ai-월간-분석-api-fastapi)

---

# Spring Boot API (데이터 관리)

## 1. 인증 API (Spring Boot)

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
  "birthDate": "YYYY-MM-DD",
  "calendarType": "solar",
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
  "birthDate": "YYYY-MM-DD",
  "gender": "M",
  "calendarType": "solar",
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

## 2. 꿈 일기 API (Spring Boot)

### 2.1 꿈 일기 작성

- **URL**: `/api/dreams`
- **Method**: `POST`
- **Description**: 새로운 꿈 일기 작성
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
  "message": "꿈 일기가 저장되었습니다."
}
```

- **Status Codes**:
  - `201 Created`: 작성 성공
  - `400 Bad Request`: 잘못된 입력 데이터
  - `401 Unauthorized`: 인증 실패

> **📌 참고**: 꿈 일기 작성 후, 프론트엔드에서 FastAPI의 `/api/v1/fortune/comprehensive`를 직접 호출하여 AI 분석을 받고, 결과를 Spring Boot의 `/api/dreams/{dreamId}/result`로 저장해야 합니다.

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

---

## 3. AI 분석 결과 저장 API (Spring Boot)

### 3.1 AI 분석 결과 저장

- **URL**: `/api/dreams/{dreamId}/result`
- **Method**: `POST`
- **Description**: FastAPI에서 받은 AI 분석 결과 저장
- **Headers**: `Authorization: Bearer {token}`
- **Path Parameters**:
  - `dreamId`: 꿈 일기 ID
- **Request Body**:

```json
{
  "dreamInterpretation": "AI 해몽 결과 텍스트",
  "todayFortuneSummary": "오늘의 운세 종합 텍스트",
  "luckyColor": {
    "name": "파란색",
    "number": 3,
    "reason": "색상 추천 이유"
  },
  "luckyItem": {
    "name": "은색 반지",
    "reason": "아이템 추천 이유"
  },
  "imageUrl": "string (optional)"
}
```

- **Response**:

```json
{
  "resultId": 1,
  "dreamId": 1,
  "message": "AI 분석 결과가 저장되었습니다."
}
```

- **Status Codes**:
  - `201 Created`: 저장 성공
  - `400 Bad Request`: 잘못된 입력 데이터
  - `401 Unauthorized`: 인증 실패
  - `404 Not Found`: 꿈 일기를 찾을 수 없음
  - `409 Conflict`: 이미 분석 결과가 존재함

### 3.2 AI 분석 결과 조회

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
  "dreamInterpretation": "AI 해몽 결과 텍스트",
  "todayFortuneSummary": "오늘의 운세 종합 텍스트",
  "luckyColor": {
    "name": "파란색",
    "number": 3,
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

### 3.3 AI 분석 결과 수정

- **URL**: `/api/dreams/{dreamId}/result`
- **Method**: `PUT`
- **Description**: AI 분석 결과 수정 (이미지 URL 업데이트 등)
- **Headers**: `Authorization: Bearer {token}`
- **Path Parameters**:
  - `dreamId`: 꿈 일기 ID
- **Request Body**:

```json
{
  "imageUrl": "string"
}
```

- **Response**:

```json
{
  "message": "AI 분석 결과가 수정되었습니다."
}
```

- **Status Codes**:
  - `200 OK`: 수정 성공
  - `401 Unauthorized`: 인증 실패
  - `404 Not Found`: 분석 결과를 찾을 수 없음

### 3.4 AI 분석 결과 삭제

- **URL**: `/api/dreams/{dreamId}/result`
- **Method**: `DELETE`
- **Description**: AI 분석 결과 삭제 (재분석을 위한 초기화)
- **Headers**: `Authorization: Bearer {token}`
- **Path Parameters**:
  - `dreamId`: 꿈 일기 ID
- **Response**:

```json
{
  "message": "AI 분석 결과가 삭제되었습니다."
}
```

- **Status Codes**:
  - `200 OK`: 삭제 성공
  - `401 Unauthorized`: 인증 실패
  - `404 Not Found`: 분석 결과를 찾을 수 없음

---

## 4. 감정 점수 API (Spring Boot)

### 4.1 감정 점수 목록 조회

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
      "emotionName": "만족",
      "score": 4
    },
    {
      "emotionId": 3,
      "emotionName": "평범",
      "score": 3
    },
    {
      "emotionId": 4,
      "emotionName": "불안",
      "score": 2
    },
    {
      "emotionId": 5,
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

## 5. 월별 분석 API (Spring Boot)

### 5.1 월별 꿈 통계 조회

- **URL**: `/api/analysis/monthly`
- **Method**: `GET`
- **Description**: 특정 월의 꿈 통계 및 기본 분석 결과 조회
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
  "monthlyReport": "월간 AI 분석 리포트 전문 (optional)",
  "createdDate": "YYYY-MM-DD HH:mm:ss",
  "updatedDate": "YYYY-MM-DD HH:mm:ss"
}
```

- **Status Codes**:
  - `200 OK`: 조회 성공
  - `401 Unauthorized`: 인증 실패
  - `404 Not Found`: 분석 데이터 없음

### 5.2 월별 AI 리포트 생성 요청

- **URL**: `/api/analysis/monthly`
- **Method**: `POST`
- **Description**: 특정 월의 AI 리포트 생성 요청 (Spring Boot가 FastAPI를 호출)
- **Headers**: `Authorization: Bearer {token}`
- **Request Body**:

```json
{
  "year": 2025,
  "month": 11
}
```

- **처리 과정**:

  1. Spring Boot가 해당 월의 꿈 일기 및 AI 결과 조회 (MySQL)
  2. Spring Boot가 FastAPI의 `/api/v1/fortune/monthly-analysis` 호출
  3. FastAPI가 AI 리포트 생성 후 반환
  4. Spring Boot가 리포트를 `dream_monthly_analysis` 테이블에 저장

- **Response**:

```json
{
  "analysisId": 1,
  "year": 2025,
  "month": 11,
  "dreamCount": 15,
  "avgEmotionScore": 3.5,
  "monthlyReport": "AI가 생성한 월간 리포트 전문",
  "message": "월별 AI 리포트가 생성되었습니다."
}
```

- **Status Codes**:
  - `200 OK`: 갱신 성공
  - `201 Created`: 생성 성공
  - `400 Bad Request`: 해당 월에 꿈 일기가 없음
  - `401 Unauthorized`: 인증 실패
  - `503 Service Unavailable`: FastAPI 서비스 오류

> **📌 참고**: 프론트엔드는 이 API만 호출하면 됩니다. Spring Boot가 자동으로 FastAPI를 호출하여 AI 리포트를 생성합니다.

---

## 6. 월별 메모 API (Spring Boot)

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

# FastAPI (AI 서비스)

## 7. AI 통합 운세 API (FastAPI)

### 7.1 통합 운세 분석

- **URL**: `{FASTAPI_URL}/api/v1/fortune/comprehensive`
- **Method**: `POST`
- **Description**: 꿈 해몽, 네이버 운세, 종합 분석, 행운의 색/아이템 제공
- **인증**: 불필요 (프론트에서 직접 호출)
- **Process**:

  1. 로컬 LLM으로 꿈 해몽
  2. 네이버 운세 크롤링 (생년월일 기반)
  3. Upstage API로 종합 분석 및 행운의 색상/아이템 추천

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
  "dream_interpretation": "꿈 해몽 결과 텍스트",
  "today_fortune_summary": "오늘의 운세 종합 요약",
  "lucky_color": {
    "name": "색상 이름",
    "number": 3,
    "reason": "해당 색상을 추천하는 이유"
  },
  "lucky_item": {
    "name": "아이템 이름",
    "reason": "해당 아이템을 추천하는 이유"
  }
}
```

**필드 설명:**

- `dream_interpretation`: 로컬 LLM의 꿈 해몽 결과
- `today_fortune_summary`: 네이버 운세 + 꿈 해몽을 종합한 오늘의 운세
- `lucky_color`: 오늘의 행운의 색
  - `name`: 색상 이름 (한글)
  - `number`: 색상 번호 (1-7)
  - `reason`: 추천 이유
- `lucky_item`: 오늘의 행운의 아이템

  - `name`: 아이템 이름
  - `reason`: 추천 이유

- **Status Codes**:
  - `200 OK`: 정상 응답
  - `422 Unprocessable Entity`: 유효성 검증 실패
  - `500 Internal Server Error`: AI 서비스 오류

---

## 8. AI 이미지 생성 API (FastAPI)

### 8.1 꿈 이미지 생성

- **URL**: `{FASTAPI_URL}/api/v1/dream/generate-image`
- **Method**: `POST`
- **Description**: 꿈 내용을 기반으로 AI 이미지 생성
- **Model**: Gemini 2.0 Flash Exp Image Generation
- **인증**: 불필요 (프론트에서 직접 호출)

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

> **📌 참고**: 프론트에서 Base64 이미지를 받아 S3 등에 업로드한 후, Spring Boot의 `/api/dreams/{dreamId}/result`로 이미지 URL을 저장합니다.

- **Status Codes**:
  - `200 OK`: 정상 응답
  - `422 Unprocessable Entity`: 유효성 검증 실패
  - `500 Internal Server Error`: 이미지 생성 실패

---

## 9. AI 월간 분석 API (FastAPI)

### 9.1 월간 꿈 리포트 생성

- **URL**: `{FASTAPI_URL}/api/v1/fortune/monthly-analysis`
- **Method**: `POST`
- **Description**: 한 달 동안의 꿈과 운세 데이터를 종합 분석하여 감동적인 리포트 생성
- **호출자**: Spring Boot (서버 간 통신)
- **인증**: 불필요
- **Process**:

  1. Spring Boot로부터 31일간의 꿈과 운세 데이터 수신
  2. 감정 흐름 및 패턴 분석
  3. 심리적 통찰 제공
  4. 다음 달을 위한 조언 제공
  5. 마크다운 형식의 리포트 생성

- **Request Body**:

```json
{
  "user_name": "string",
  "birth_date": "YYYY-MM-DD",
  "daily_data": [
    {
      "date": "YYYY-MM-DD",
      "dream_content": "string",
      "today_fortune_summary": "string",
      "emotion_score": 3
    }
  ]
}
```

**필드 설명:**

- `user_name`: 사용자 이름
- `birth_date`: 생년월일
- `daily_data`: 일별 데이터 배열 (최대 31일)

  - `date`: 날짜 (YYYY-MM-DD)
  - `dream_content`: 꿈 내용
  - `today_fortune_summary`: 오늘의 운세 요약
  - `emotion_score`: 감정 점수 (1-5)

- **Response (200 OK)**:

```json
{
  "period": "YYYY-MM",
  "user_name": "string",
  "report": "월간 분석 리포트 전문 (마크다운 형식)",
  "statistics": {
    "total_dreams": 15,
    "avg_emotion_score": 3.5,
    "emotion_distribution": {
      "1": 2,
      "2": 3,
      "3": 5,
      "4": 3,
      "5": 2
    },
    "most_common_themes": ["성장", "관계", "도전"]
  },
  "metadata": {
    "generated_at": "YYYY-MM-DD HH:mm:ss",
    "model": "solar-pro",
    "version": "1.0"
  }
}
```

**필드 설명:**

- `period`: 분석 기간 (YYYY-MM)
- `user_name`: 사용자 이름
- `report`: 월간 분석 리포트 전문 (마크다운 형식, 프론트에서 렌더링)
- `statistics`: 통계 정보
  - `total_dreams`: 총 꿈 개수
  - `avg_emotion_score`: 평균 감정 점수
  - `emotion_distribution`: 감정 점수 분포
  - `most_common_themes`: 주요 테마
- `metadata`: 메타데이터
  - `generated_at`: 생성 시각
  - `model`: 사용된 AI 모델
  - `version`: API 버전

> **📌 참고**: 이 API는 Spring Boot에서만 호출됩니다. 프론트엔드는 Spring Boot의 `/api/analysis/monthly`를 호출하면 됩니다.

- **Status Codes**:
  - `200 OK`: 정상 응답
  - `422 Unprocessable Entity`: 유효성 검증 실패
  - `500 Internal Server Error`: 분석 생성 실패

---

## 프론트엔드 통신 플로우

### 시나리오 1: 꿈 일기 작성 및 AI 분석

```
1. [프론트] → [Spring Boot] POST /api/dreams
   꿈 일기 저장
   ↓
   Response: { dreamId, ... }

2. [프론트] → [FastAPI] POST /api/v1/fortune/comprehensive
   AI 운세 분석 요청
   ↓
   Response: { dream_interpretation, today_fortune_summary, lucky_color, lucky_item }

3. [프론트] → [Spring Boot] POST /api/dreams/{dreamId}/result
   AI 분석 결과 저장
   ↓
   Response: { resultId, message }

4. [프론트] 사용자에게 완료 메시지 표시
```

### 시나리오 2: 꿈 이미지 생성

```
1. [프론트] → [FastAPI] POST /api/v1/dream/generate-image
   이미지 생성 요청
   ↓
   Response: { images: [{ image_data, mime_type }] }

2. [프론트] Base64 이미지를 Blob으로 변환 후 S3 업로드
   ↓
   imageUrl 획득

3. [프론트] → [Spring Boot] PUT /api/dreams/{dreamId}/result
   이미지 URL 업데이트
   ↓
   Response: { message }
```

### 시나리오 3: 월간 리포트 생성

```
1. [프론트] → [Spring Boot] POST /api/analysis/monthly
   월간 AI 리포트 생성 요청 (year, month 포함)
   ↓

2. [Spring Boot] 내부 처리:
   - MySQL에서 해당 월의 꿈 일기 조회
   - 각 꿈의 AI 분석 결과 조회
   - daily_data 배열 구성
   ↓

3. [Spring Boot] → [FastAPI] POST /api/v1/fortune/monthly-analysis
   AI 리포트 생성 요청
   ↓

4. [FastAPI] → [Spring Boot]
   AI 리포트 반환 (report, statistics, metadata)
   ↓

5. [Spring Boot]
   - MySQL에 월간 리포트 저장
   - 통계 정보 업데이트
   ↓

6. [프론트] ← [Spring Boot]
   완료 응답 (analysisId, dreamCount, avgEmotionScore, monthlyReport)
```

**장점:**

- 프론트엔드 로직이 단순함 (1번의 API 호출만)
- 데이터가 서버 간에만 이동 (보안 향상)
- 트랜잭션 관리가 용이함
- 에러 핸들링이 일관적

---

## CORS 및 보안 설정

### FastAPI CORS 설정

```python
from fastapi.middleware.cors import CORSMiddleware

app.add_middleware(
    CORSMiddleware,
    allow_origins=["http://localhost:3000", "https://your-frontend-domain.com"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)
```

### Spring Boot CORS 설정

```java
@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins("http://localhost:3000", "https://your-frontend-domain.com")
                .allowedMethods("GET", "POST", "PUT", "DELETE")
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}
```

### 인증 처리

- **Spring Boot**: JWT 토큰 기반 인증
- **FastAPI**: 인증 불필요 (공개 AI 서비스)
- 프론트엔드에서 Spring Boot API 호출 시에만 `Authorization: Bearer {token}` 헤더 추가

---

## 공통 사항

### 에러 응답 형식

**Spring Boot:**

```json
{
  "error": "error_code",
  "message": "에러 메시지",
  "timestamp": "YYYY-MM-DD HH:mm:ss"
}
```

**FastAPI:**

```json
{
  "detail": "에러 메시지"
}
```

### 공통 Status Codes

- `400 Bad Request`: 잘못된 요청
- `401 Unauthorized`: 인증 토큰이 없거나 유효하지 않음 (Spring Boot만)
- `403 Forbidden`: 접근 권한 없음
- `404 Not Found`: 리소스를 찾을 수 없음
- `422 Unprocessable Entity`: 유효성 검증 실패 (FastAPI)
- `500 Internal Server Error`: 서버 오류
- `503 Service Unavailable`: 외부 서비스 오류

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
- dream_id (BIGINT, FK, NOT NULL, UNIQUE) → dreams(dream_id)
- dream_interpretation (TEXT, NOT NULL) - 꿈 해몽 결과
- today_fortune_summary (TEXT, NOT NULL) - 오늘의 운세 종합
- lucky_color_name (VARCHAR(50), NOT NULL) - 행운의 색 이름
- lucky_color_number (INT, NOT NULL) - 행운의 색 번호 (1-7)
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

**초기 데이터:**

```sql
INSERT INTO emotion_scores (emotion_id, emotion_name, score) VALUES
(1, '기쁨', 5),
(2, '만족', 4),
(3, '평범', 3),
(4, '불안', 2),
(5, '슬픔', 1);
```

### dream_monthly_analysis (월별 꿈 분석)

- analysis_id (BIGINT, PK, NOT NULL)
- user_id (BIGINT, FK, NOT NULL) → users(user_id)
- year (INT, NOT NULL)
- month (INT, NOT NULL)
- dream_count (INT, NOT NULL)
- avg_emotion_score (DECIMAL(5,2), NOT NULL)
- monthly_report (LONGTEXT, NULL) - FastAPI에서 생성한 월간 리포트
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

### Spring Boot application.properties

```properties
# Database
spring.datasource.url=jdbc:mysql://localhost:3306/dream_db?useSSL=false&serverTimezone=UTC&characterEncoding=UTF-8
spring.datasource.username=your_username
spring.datasource.password=your_password
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# JPA
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQL8Dialect

# JWT
jwt.secret=your_secret_key_here_at_least_256_bits
jwt.expiration=86400000

# File Upload (이미지 업로드용, S3 사용 시 불필요)
spring.servlet.multipart.max-file-size=10MB
spring.servlet.multipart.max-request-size=10MB

# Logging
logging.level.org.springframework.web=INFO
logging.level.com.yourpackage=DEBUG

# FastAPI 연동 설정
fastapi.url=http://localhost:8000
fastapi.timeout=60000
```

### Spring Boot에서 FastAPI 호출 설정

**RestTemplate 설정 (Spring Boot):**

```java
@Configuration
public class RestTemplateConfig {

    @Value("${fastapi.url}")
    private String fastApiUrl;

    @Value("${fastapi.timeout:60000}")
    private int timeout;

    @Bean
    public RestTemplate restTemplate() {
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
        factory.setConnectTimeout(timeout);
        factory.setReadTimeout(timeout);

        return new RestTemplate(factory);
    }
}
```

**또는 WebClient 사용 (비동기 처리 권장):**

```java
@Configuration
public class WebClientConfig {

    @Value("${fastapi.url}")
    private String fastApiUrl;

    @Value("${fastapi.timeout:60000}")
    private int timeout;

    @Bean
    public WebClient webClient() {
        return WebClient.builder()
            .baseUrl(fastApiUrl)
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .codecs(configurer -> configurer
                .defaultCodecs()
                .maxInMemorySize(16 * 1024 * 1024)) // 16MB
            .build();
    }
}
```

### FastAPI 환경 변수 (.env)

```env
# GMS API Key (Upstage Solar)
GMS_API_KEY=your_gms_api_key_here

# Gemini API Key (이미지 생성용)
GOOGLE_API_KEY=your_gemini_api_key_here

# Server Settings
HOST=0.0.0.0
PORT=8000

# CORS Allowed Origins (쉼표로 구분)
CORS_ORIGINS=http://localhost:3000,https://your-frontend-domain.com
```

### FastAPI 서버 실행

```bash
cd /Users/kimhyunbin/Desktop/github_ssafy/AI-api-service
python main.py
```

또는

```bash
uvicorn main:app --host 0.0.0.0 --port 8000 --reload
```

---

## API 문서 및 테스트

### FastAPI Swagger UI

- **URL**: `http://localhost:8000/docs`
- 모든 FastAPI 엔드포인트를 대화형으로 테스트 가능

### FastAPI OpenAPI Schema

- **URL**: `http://localhost:8000/openapi.json`

---

## 참고 사항

1. 모든 날짜는 `YYYY-MM-DD` 형식 사용
2. 모든 날짜/시간은 `YYYY-MM-DD HH:mm:ss` 형식 사용
3. 삭제는 Soft Delete 방식 사용 (deleted_date 컬럼 활용)
4. 이미지는 Base64로 전송 후 S3 등 외부 저장소에 업로드 권장

### 프론트엔드 통신 규칙

5. **일일 운세/이미지 생성**: 프론트 → FastAPI 직접 호출 가능
6. **월간 분석**: 프론트 → Spring Boot → FastAPI (Spring Boot를 통해서만 가능) ⭐
7. Spring Boot API는 JWT 토큰 필수 (인증 API 제외)
8. 프론트엔드에서 AI 분석 결과를 받은 후 반드시 Spring Boot에 저장해야 함

### 기술적 고려사항

9. 월간 리포트는 마크다운 형식으로 제공되므로 프론트에서 렌더링 필요
10. FastAPI 서버는 GPU 사용을 위해 CUDA 환경 권장 (LLM 모델 로드)
11. Spring Boot에서 FastAPI 호출 시 타임아웃 설정 필수 (60초 권장)
12. 프로덕션 환경에서는 FastAPI와 Spring Boot 모두 HTTPS 사용 권장
13. Spring Boot는 RestTemplate 또는 WebClient로 FastAPI 호출 (WebClient 비동기 처리 권장)

---

## 배포 고려사항

### FastAPI 배포

1. **Docker 컨테이너화**

   ```dockerfile
   FROM python:3.10-slim
   WORKDIR /app
   COPY requirements.txt .
   RUN pip install --no-cache-dir -r requirements.txt
   COPY . .
   CMD ["uvicorn", "main:app", "--host", "0.0.0.0", "--port", "8000"]
   ```

2. **GPU 지원** (LLM 모델용)

   - NVIDIA Docker Runtime 사용
   - CUDA 11.8 이상 권장

3. **로드 밸런싱**
   - AI 처리 시간이 길 수 있으므로 타임아웃 설정 필수
   - Nginx 리버스 프록시 권장

### Spring Boot 배포

1. **JAR 빌드**

   ```bash
   ./mvnw clean package
   java -jar target/app.jar
   ```

2. **환경 변수 분리**
   - DB 연결 정보
   - JWT Secret Key
   - **FastAPI URL (환경별 다름)** ⭐
     - 개발: `http://localhost:8000`
     - 스테이징: `http://fastapi-staging:8000`

**환경별 FastAPI URL 설정 예시:**

```bash
# 개발 환경
java -jar app.jar --fastapi.url=http://localhost:8000

# 프로덕션 환경
java -jar app.jar --fastapi.url=https://api-ai.your-domain.com
```

---

## 문의 및 지원

- FastAPI 서버 상태 확인: `GET {FASTAPI_URL}/`
- FastAPI 문서: `{FASTAPI_URL}/docs`
