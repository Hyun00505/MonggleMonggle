# 🌙 몽글몽글 (MonggleMonggle) - 꿈 일기 & AI 해몽 서비스

> **SSAFY Final Project** - 캘린더 기반 꿈 일기 작성 및 AI 해몽, 운세, 행운의 색 제공 서비스

<div align="center">

![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.8-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)
![Java](https://img.shields.io/badge/Java-17-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![MySQL](https://img.shields.io/badge/MySQL-8.0-4479A1?style=for-the-badge&logo=mysql&logoColor=white)
![MyBatis](https://img.shields.io/badge/MyBatis-3.0.3-DC382D?style=for-the-badge)
![JWT](https://img.shields.io/badge/JWT-0.12.3-000000?style=for-the-badge&logo=jsonwebtokens&logoColor=white)

</div>

---

## 📚 목차

1. [프로젝트 개요](#-프로젝트-개요)
2. [기술 스택](#-기술-스택)
3. [시스템 아키텍처](#-시스템-아키텍처)
4. [프로젝트 구조](#-프로젝트-구조)
5. [데이터베이스 설계](#-데이터베이스-설계)
6. [API 명세서](#-api-명세서)
7. [환경 설정](#-환경-설정)
8. [실행 방법](#-실행-방법)
9. [주요 기능 상세](#-주요-기능-상세)
10. [보안](#-보안)
11. [코드 상세 설명](#-코드-상세-설명)

---

## 🎯 프로젝트 개요

### 서비스 소개

**몽글몽글**은 사용자가 매일의 꿈을 기록하고, AI를 통해 꿈 해몽과 오늘의 운세를 받아볼 수 있는 서비스입니다.

### 주요 기능

| 기능 | 설명 |
|------|------|
| 🔐 **회원 관리** | 회원가입, 로그인, 정보 수정, 회원 탈퇴 (Soft Delete) |
| 📖 **꿈 일기** | 꿈 일기 작성, 조회, 수정, 삭제 (월별/상세) |
| 🤖 **AI 분석** | FastAPI 연동 꿈 해몽, 운세, 행운의 색/아이템 제공 |
| 📊 **월별 분석** | 월별 꿈 통계 및 AI 리포트 생성 |
| 📝 **월별 메모** | 월별 개인 메모 작성 |
| 🖼️ **이미지 관리** | Base64 이미지 업로드 및 삭제 |
| 😊 **감정 점수** | 5가지 감정 (기쁨/만족/평범/불안/슬픔) |

---

## 🛠 기술 스택

### Backend Framework
```
Spring Boot 3.5.8
├── spring-boot-starter-web          # REST API
├── spring-boot-starter-security     # 보안 (JWT 인증)
├── spring-boot-starter-validation   # Bean Validation
├── spring-boot-starter-webflux      # WebClient (FastAPI 연동)
└── spring-boot-devtools             # 개발 편의
```

### Database & ORM
```
MySQL 8.0
└── MyBatis 3.0.3
    ├── Mapper XML
    └── Type Aliases
```

### Security
```
Spring Security 6.x
├── JWT (jjwt 0.12.3)
│   ├── jjwt-api
│   ├── jjwt-impl
│   └── jjwt-jackson
└── BCrypt Password Encoder
```

### Documentation
```
Swagger/OpenAPI
└── springdoc-openapi-starter-webmvc-ui 2.7.0
```

### Utilities
```
Lombok          # 보일러플레이트 코드 제거
Gradle          # 빌드 도구
```

---

## 🏗 시스템 아키텍처

### 전체 워크플로우

```
┌──────────────┐     HTTP/REST      ┌──────────────────┐
│   Frontend   │◄──────────────────►│   Spring Boot    │
│   (Vue.js)   │     JSON + JWT     │   Backend (8080) │
└──────────────┘                    └────────┬─────────┘
                                             │
                    ┌────────────────────────┼────────────────────────┐
                    │                        │                        │
                    ▼                        ▼                        ▼
           ┌───────────────┐        ┌───────────────┐        ┌───────────────┐
           │    MySQL      │        │   FastAPI     │        │  File System  │
           │  (dream_db)   │        │ AI Server     │        │   (uploads/)  │
           └───────────────┘        │   (8000)      │        └───────────────┘
                                    └───────────────┘
```

### API 연동 워크플로우

```
┌────────────────────────────────────────────────────────────────┐
│                        AI 분석 플로우                          │
├────────────────────────────────────────────────────────────────┤
│                                                                │
│   1. 꿈 일기 작성                                              │
│      └─► POST /api/dreams                                      │
│                                                                │
│   2. AI 분석 요청 (Frontend → FastAPI)                         │
│      └─► FastAPI가 꿈 내용 분석 후 결과 반환                   │
│                                                                │
│   3. AI 분석 결과 저장                                         │
│      └─► POST /api/dreams/{dreamId}/result                     │
│          (해몽, 운세, 행운의 색/아이템)                        │
│                                                                │
│   4. 이미지 업로드 (선택)                                      │
│      └─► POST /api/images/upload                               │
│          (AI 생성 이미지 Base64 저장)                          │
│                                                                │
└────────────────────────────────────────────────────────────────┘
```

---

## 📁 프로젝트 구조

```
BACK/
├── 📄 build.gradle                    # Gradle 빌드 설정
├── 📄 settings.gradle                 # Gradle 설정
├── 📄 dream_DB.sql                    # 데이터베이스 스키마
├── 📄 gradlew / gradlew.bat           # Gradle Wrapper
├── 📁 gradle/
│   └── wrapper/
│       ├── gradle-wrapper.jar
│       └── gradle-wrapper.properties
├── 📁 img/                            # 문서용 이미지
│   ├── AI_API_Workflow.png
│   ├── Database.png
│   ├── Total_workflow.png
│   └── Usercase.png
├── 📁 uploads/                        # 업로드 이미지 저장소
│   └── images/
│       └── dream/
│           └── {userId}/              # 사용자별 폴더
└── 📁 src/
    ├── 📁 main/
    │   ├── 📁 java/com/ssafy/finalproject/
    │   │   ├── 📄 FinalProjectApplication.java      # 메인 클래스
    │   │   │
    │   │   ├── 📁 config/                           # 설정 클래스
    │   │   │   ├── SecurityConfig.java              # Spring Security 설정
    │   │   │   ├── SwaggerConfig.java               # Swagger/OpenAPI 설정
    │   │   │   ├── WebClientConfig.java             # WebClient 설정
    │   │   │   └── WebConfig.java                   # CORS, 리소스 핸들러
    │   │   │
    │   │   ├── 📁 controller/                       # REST 컨트롤러
    │   │   │   ├── AuthController.java              # 인증 API
    │   │   │   ├── DreamController.java             # 꿈 일기 API
    │   │   │   ├── DreamResultController.java       # AI 분석 결과 API
    │   │   │   ├── EmotionController.java           # 감정 점수 API
    │   │   │   ├── ImageController.java             # 이미지 API
    │   │   │   ├── MonthlyAnalysisController.java   # 월별 분석 API
    │   │   │   └── MonthlyMemoController.java       # 월별 메모 API
    │   │   │
    │   │   ├── 📁 service/                          # 비즈니스 로직
    │   │   │   ├── AuthService.java
    │   │   │   ├── DreamService.java
    │   │   │   ├── DreamResultService.java
    │   │   │   ├── EmotionService.java
    │   │   │   ├── ImageService.java
    │   │   │   ├── MonthlyAnalysisService.java
    │   │   │   └── MonthlyMemoService.java
    │   │   │
    │   │   ├── 📁 model/
    │   │   │   ├── 📁 entity/                       # 도메인 엔티티
    │   │   │   │   ├── User.java                    # 사용자
    │   │   │   │   ├── Dream.java                   # 꿈 일기
    │   │   │   │   ├── DreamResult.java             # AI 분석 결과
    │   │   │   │   ├── EmotionScore.java            # 감정 점수
    │   │   │   │   ├── MonthlyAnalysis.java         # 월별 분석
    │   │   │   │   └── MonthlyMemo.java             # 월별 메모
    │   │   │   │
    │   │   │   ├── 📁 dao/                          # MyBatis Mapper 인터페이스
    │   │   │   │   ├── UserDao.java
    │   │   │   │   ├── DreamsDao.java
    │   │   │   │   ├── DreamsResultsDao.java
    │   │   │   │   ├── EmotionDao.java
    │   │   │   │   ├── MonthlyAnalysisDao.java
    │   │   │   │   └── MonthlyMemoDao.java
    │   │   │   │
    │   │   │   └── 📁 dto/
    │   │   │       ├── 📁 request/                  # 요청 DTO
    │   │   │       │   ├── SignupRequest.java
    │   │   │       │   ├── LoginRequest.java
    │   │   │       │   ├── UpdateUserRequest.java
    │   │   │       │   ├── CreateDreamRequest.java
    │   │   │       │   ├── UpdateDreamRequest.java
    │   │   │       │   ├── SaveDreamResultRequest.java
    │   │   │       │   ├── UpdateDreamResultRequest.java
    │   │   │       │   ├── MonthlyAnalysisRequest.java
    │   │   │       │   └── SaveMemoRequest.java
    │   │   │       │
    │   │   │       ├── 📁 response/                 # 응답 DTO
    │   │   │       │   ├── ApiResponse.java
    │   │   │       │   ├── ErrorResponse.java
    │   │   │       │   ├── SignupResponse.java
    │   │   │       │   ├── LoginResponse.java
    │   │   │       │   ├── UserInfoResponse.java
    │   │   │       │   ├── DreamResponse.java
    │   │   │       │   ├── DreamListResponse.java
    │   │   │       │   ├── DreamResultResponse.java
    │   │   │       │   ├── EmotionListResponse.java
    │   │   │       │   ├── MonthlyAnalysisResponse.java
    │   │   │       │   └── MonthlyMemoResponse.java
    │   │   │       │
    │   │   │       └── 📁 fastapi/                  # FastAPI 연동 DTO
    │   │   │           ├── MonthlyAnalysisRequestDto.java
    │   │   │           └── MonthlyAnalysisResponseDto.java
    │   │   │
    │   │   ├── 📁 security/                         # 보안 컴포넌트
    │   │   │   ├── JwtUtil.java                     # JWT 토큰 유틸
    │   │   │   └── JwtAuthenticationFilter.java     # JWT 인증 필터
    │   │   │
    │   │   ├── 📁 exception/                        # 예외 처리
    │   │   │   ├── CustomException.java             # 기본 예외 클래스
    │   │   │   ├── BadRequestException.java         # 400
    │   │   │   ├── UnauthorizedException.java       # 401
    │   │   │   ├── ForbiddenException.java          # 403
    │   │   │   ├── ResourceNotFoundException.java   # 404
    │   │   │   ├── ConflictException.java           # 409
    │   │   │   ├── ServiceUnavailableException.java # 503
    │   │   │   └── GlobalExceptionHandler.java      # 전역 예외 핸들러
    │   │   │
    │   │   └── 📁 util/
    │   │       └── SecurityUtil.java                # 보안 유틸리티
    │   │
    │   └── 📁 resources/
    │       ├── 📄 application.yaml                  # 애플리케이션 설정
    │       ├── 📄 mybatis-config.xml                # MyBatis 설정
    │       └── 📁 mapper/                           # MyBatis Mapper XML
    │           ├── 📁 user/
    │           │   └── UserMapper.xml
    │           ├── 📁 dream/
    │           │   ├── DreamsMapper.xml
    │           │   ├── DreamResultsMapper.xml
    │           │   └── EmotionMapper.xml
    │           └── 📁 monthly/
    │               ├── MonthlyAnalysisMapper.xml
    │               └── MonthlyMemoMapper.xml
    │
    └── 📁 test/                                     # 테스트 코드
        └── java/com/ssafy/finalproject/
            └── FinalProjectApplicationTests.java
```

---

## 🗄 데이터베이스 설계

### ERD (Entity Relationship Diagram)

```
┌───────────────────────────────────────────────────────────────────────────┐
│                              dream_db                                     │
└───────────────────────────────────────────────────────────────────────────┘

┌─────────────────────┐       ┌─────────────────────┐
│       users         │       │   emotion_scores    │
├─────────────────────┤       ├─────────────────────┤
│ PK user_id (BIGINT) │       │ PK emotion_id (INT) │
│    login_id         │       │    emotion_name     │
│    password         │       │    score            │
│    name             │       └──────────┬──────────┘
│    birth_date       │                  │
│    gender           │                  │
│    calendar_type    │                  │
│    created_date     │                  │
│    updated_date     │                  │
│    deleted_date     │                  │
└──────────┬──────────┘                  │
           │                             │
           │ 1:N                         │ 1:N
           │                             │
           ▼                             ▼
┌─────────────────────────────────────────────────┐
│                    dreams                       │
├─────────────────────────────────────────────────┤
│ PK dream_id (BIGINT)                            │
│ FK user_id (BIGINT) ─────────────────► users    │
│ FK emotion_id (INT) ─────────────────► emotion  │
│    dream_date                                   │
│    title                                        │
│    content                                      │
│    created_date                                 │
│    deleted_date                                 │
└──────────┬──────────────────────────────────────┘
           │
           │ 1:1
           │
           ▼
┌─────────────────────────────────────────────────┐
│                dream_results                    │
├─────────────────────────────────────────────────┤
│ PK id (BIGINT)                                  │
│ FK dream_id (BIGINT) ────────────────► dreams   │
│    dream_interpretation                         │
│    today_fortune_summary                        │
│    lucky_color_name                             │
│    lucky_color_number                           │
│    lucky_color_reason                           │
│    lucky_item_name                              │
│    lucky_item_reason                            │
│    image_url                                    │
│    created_date                                 │
│    deleted_date                                 │
└─────────────────────────────────────────────────┘

┌─────────────────────┐       ┌─────────────────────┐
│ users               │       │ dream_monthly_memo  │
└──────────┬──────────┘       └──────────┬──────────┘
           │                             │
           │ 1:N                         │ 1:1
           │                             │
           ▼                             ▼
┌─────────────────────────────────────────────────┐
│            dream_monthly_analysis               │
├─────────────────────────────────────────────────┤
│ PK analysis_id (BIGINT)                         │
│ FK user_id (BIGINT) ─────────────────► users    │
│    year                                         │
│    month                                        │
│    dream_count                                  │
│    avg_emotion_score                            │
│    monthly_report (LONGTEXT)                    │
│    created_date                                 │
│    updated_date                                 │
└─────────────────────────────────────────────────┘
```

### 테이블 상세 설명

#### 1. users (회원 정보)
| 컬럼명 | 타입 | 설명 | 비고 |
|--------|------|------|------|
| user_id | BIGINT | 사용자 ID | PK, AUTO_INCREMENT |
| login_id | VARCHAR(255) | 로그인 아이디 | UNIQUE |
| password | VARCHAR(255) | 비밀번호 | BCrypt 암호화 |
| name | VARCHAR(100) | 이름 | |
| birth_date | DATE | 생년월일 | |
| gender | CHAR(1) | 성별 | 'M' 또는 'F' |
| calendar_type | VARCHAR(20) | 달력 유형 | solar/lunarGeneral/lunarLeap |
| created_date | DATETIME | 가입일 | |
| updated_date | DATETIME | 수정일 | |
| deleted_date | DATETIME | 삭제일 | Soft Delete |

#### 2. emotion_scores (감정 점수)
| 컬럼명 | 타입 | 설명 | 비고 |
|--------|------|------|------|
| emotion_id | TINYINT | 감정 ID | PK |
| emotion_name | VARCHAR(20) | 감정 이름 | 기쁨/만족/평범/불안/슬픔 |
| score | INT | 감정 점수 | 1~5 |

**초기 데이터:**
| emotion_id | emotion_name | score |
|------------|--------------|-------|
| 1 | 기쁨 | 5 |
| 2 | 만족 | 4 |
| 3 | 평범 | 3 |
| 4 | 불안 | 2 |
| 5 | 슬픔 | 1 |

#### 3. dreams (꿈 기록)
| 컬럼명 | 타입 | 설명 | 비고 |
|--------|------|------|------|
| dream_id | BIGINT | 꿈 ID | PK, AUTO_INCREMENT |
| user_id | BIGINT | 사용자 ID | FK → users |
| emotion_id | TINYINT | 감정 ID | FK → emotion_scores |
| dream_date | DATE | 꿈 꾼 날짜 | |
| title | TEXT | 꿈 제목 | |
| content | TEXT | 꿈 내용 | |
| created_date | DATETIME | 생성일 | |
| deleted_date | DATETIME | 삭제일 | Soft Delete |

#### 4. dream_results (AI 분석 결과)
| 컬럼명 | 타입 | 설명 | 비고 |
|--------|------|------|------|
| id | BIGINT | 결과 ID | PK, AUTO_INCREMENT |
| dream_id | BIGINT | 꿈 ID | FK → dreams, UNIQUE |
| dream_interpretation | TEXT | AI 꿈 해몽 결과 | |
| today_fortune_summary | TEXT | 오늘의 운세 종합 | |
| lucky_color_name | VARCHAR(50) | 행운의 색 이름 | |
| lucky_color_number | INT | 행운의 색 번호 | 1~7 |
| lucky_color_reason | TEXT | 행운의 색 추천 이유 | |
| lucky_item_name | VARCHAR(100) | 행운의 아이템 이름 | |
| lucky_item_reason | TEXT | 행운의 아이템 추천 이유 | |
| image_url | VARCHAR(255) | 꿈 이미지 URL | Nullable |
| created_date | DATETIME | 생성일 | |
| deleted_date | DATETIME | 삭제일 | Soft Delete |

#### 5. dream_monthly_analysis (월별 분석)
| 컬럼명 | 타입 | 설명 | 비고 |
|--------|------|------|------|
| analysis_id | BIGINT | 분석 ID | PK, AUTO_INCREMENT |
| user_id | BIGINT | 사용자 ID | FK → users |
| year | INT | 연도 | |
| month | INT | 월 | |
| dream_count | INT | 꿈 개수 | |
| avg_emotion_score | DECIMAL(5,2) | 평균 감정 점수 | |
| monthly_report | LONGTEXT | AI 월간 리포트 | Markdown |
| created_date | DATETIME | 생성일 | |
| updated_date | DATETIME | 수정일 | |

#### 6. dream_monthly_memo (월별 메모)
| 컬럼명 | 타입 | 설명 | 비고 |
|--------|------|------|------|
| memo_id | BIGINT | 메모 ID | PK, AUTO_INCREMENT |
| analysis_id | BIGINT | 분석 ID | FK → monthly_analysis, UNIQUE |
| memo_content | TEXT | 메모 내용 | |
| created_date | DATETIME | 생성일 | |
| updated_date | DATETIME | 수정일 | |
| deleted_date | DATETIME | 삭제일 | Soft Delete |

---

## 📡 API 명세서

### Base URL
```
http://localhost:8080
```

### Swagger UI
```
http://localhost:8080/swagger-ui/index.html
```

---

### 1️⃣ 인증 API (`/api/auth`)

| Method | Endpoint | 설명 | 인증 |
|--------|----------|------|------|
| POST | `/api/auth/signup` | 회원가입 | ❌ |
| POST | `/api/auth/login` | 로그인 | ❌ |
| POST | `/api/auth/logout` | 로그아웃 | ✅ |
| GET | `/api/auth/me` | 사용자 정보 조회 | ✅ |
| PUT | `/api/auth/me` | 사용자 정보 수정 | ✅ |
| DELETE | `/api/auth/me` | 회원 탈퇴 | ✅ |

#### 1.1 회원가입
```http
POST /api/auth/signup
Content-Type: application/json

{
    "loginId": "testuser",
    "password": "password123",
    "name": "홍길동",
    "birthDate": "1990-01-15",
    "gender": "M",
    "calendarType": "solar"
}
```

**Response (201 Created):**
```json
{
    "userId": 1,
    "loginId": "testuser",
    "name": "홍길동",
    "gender": "M",
    "birthDate": "1990-01-15",
    "calendarType": "solar",
    "message": "회원가입이 완료되었습니다."
}
```

#### 1.2 로그인
```http
POST /api/auth/login
Content-Type: application/json

{
    "loginId": "testuser",
    "password": "password123"
}
```

**Response (200 OK):**
```json
{
    "userId": 1,
    "loginId": "testuser",
    "name": "홍길동",
    "birthDate": "1990-01-15",
    "gender": "M",
    "calendarType": "solar",
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "message": "로그인 성공"
}
```

---

### 2️⃣ 꿈 일기 API (`/api/dreams`)

| Method | Endpoint | 설명 | 인증 |
|--------|----------|------|------|
| POST | `/api/dreams` | 꿈 일기 작성 | ✅ |
| GET | `/api/dreams?year={year}&month={month}` | 월별 목록 조회 | ✅ |
| GET | `/api/dreams/{dreamId}` | 상세 조회 | ✅ |
| PUT | `/api/dreams/{dreamId}` | 수정 | ✅ |
| DELETE | `/api/dreams/{dreamId}` | 삭제 | ✅ |

#### 2.1 꿈 일기 작성
```http
POST /api/dreams
Authorization: Bearer {token}
Content-Type: application/json

{
    "emotionId": 1,
    "dreamDate": "2025-12-08",
    "title": "신비로운 숲 속의 꿈",
    "content": "오늘 밤 신비로운 숲 속을 걸어다녔습니다..."
}
```

**Response (201 Created):**
```json
{
    "dreamId": 1,
    "userId": 1,
    "emotionId": 1,
    "dreamDate": "2025-12-08",
    "title": "신비로운 숲 속의 꿈",
    "content": "오늘 밤 신비로운 숲 속을 걸어다녔습니다...",
    "createdDate": "2025-12-08T10:30:00"
}
```

#### 2.2 월별 목록 조회
```http
GET /api/dreams?year=2025&month=12
Authorization: Bearer {token}
```

**Response (200 OK):**
```json
{
    "year": 2025,
    "month": 12,
    "dreams": [
        {
            "dreamId": 1,
            "title": "신비로운 숲 속의 꿈",
            "content": "오늘 밤 신비로운 숲 속을...",
            "dreamDate": "2025-12-08",
            "emotionId": 1,
            "emotionName": "기쁨",
            "hasResult": true,
            "luckyColorName": "파란색",
            "luckyColorNumber": 3
        }
    ]
}
```

---

### 3️⃣ AI 분석 결과 API (`/api/dreams/{dreamId}/result`)

| Method | Endpoint | 설명 | 인증 |
|--------|----------|------|------|
| POST | `/api/dreams/{dreamId}/result` | 결과 저장 | ✅ |
| GET | `/api/dreams/{dreamId}/result` | 결과 조회 | ✅ |
| PUT | `/api/dreams/{dreamId}/result` | 결과 수정 | ✅ |
| DELETE | `/api/dreams/{dreamId}/result` | 결과 삭제 | ✅ |

#### 3.1 AI 분석 결과 저장
```http
POST /api/dreams/1/result
Authorization: Bearer {token}
Content-Type: application/json

{
    "dreamInterpretation": "숲은 내면의 탐험을 상징합니다...",
    "todayFortuneSummary": "오늘은 새로운 시작에 좋은 날입니다...",
    "luckyColor": {
        "name": "초록색",
        "number": 4,
        "reason": "자연의 힘을 받을 수 있습니다"
    },
    "luckyItem": {
        "name": "나뭇잎 모양 액세서리",
        "reason": "자연과의 연결을 강화합니다"
    },
    "imageUrl": "/uploads/images/dream/1/20251208_abc123.png"
}
```

**Response (201 Created):**
```json
{
    "resultId": 1,
    "dreamId": 1,
    "message": "AI 분석 결과가 저장되었습니다."
}
```

---

### 4️⃣ 감정 점수 API (`/api/emotions`)

| Method | Endpoint | 설명 | 인증 |
|--------|----------|------|------|
| GET | `/api/emotions` | 감정 목록 조회 | ✅ |

**Response (200 OK):**
```json
{
    "emotions": [
        { "emotionId": 1, "emotionName": "기쁨", "score": 5 },
        { "emotionId": 2, "emotionName": "만족", "score": 4 },
        { "emotionId": 3, "emotionName": "평범", "score": 3 },
        { "emotionId": 4, "emotionName": "불안", "score": 2 },
        { "emotionId": 5, "emotionName": "슬픔", "score": 1 }
    ]
}
```

---

### 5️⃣ 월별 분석 API (`/api/analysis/monthly`)

| Method | Endpoint | 설명 | 인증 |
|--------|----------|------|------|
| GET | `/api/analysis/monthly?year={year}&month={month}` | 통계 조회 | ✅ |
| POST | `/api/analysis/monthly` | AI 리포트 생성 | ✅ |

#### 5.2 월별 AI 리포트 생성
```http
POST /api/analysis/monthly
Authorization: Bearer {token}
Content-Type: application/json

{
    "year": 2025,
    "month": 12
}
```

**Response (201 Created):**
```json
{
    "analysisId": 1,
    "year": 2025,
    "month": 12,
    "dreamCount": 15,
    "avgEmotionScore": 3.67,
    "monthlyReport": "## 12월 꿈 분석 리포트\n\n이번 달은 전반적으로...",
    "createdDate": "2025-12-08T10:30:00",
    "updatedDate": null
}
```

---

### 6️⃣ 월별 메모 API (`/api/memo/monthly`)

| Method | Endpoint | 설명 | 인증 |
|--------|----------|------|------|
| GET | `/api/memo/monthly?year={year}&month={month}` | 메모 조회 | ✅ |
| POST | `/api/memo/monthly` | 메모 저장/수정 | ✅ |
| DELETE | `/api/memo/monthly/{memoId}` | 메모 삭제 | ✅ |

---

### 7️⃣ 이미지 API (`/api/images`)

| Method | Endpoint | 설명 | 인증 |
|--------|----------|------|------|
| POST | `/api/images/upload` | Base64 이미지 업로드 | ✅ |
| DELETE | `/api/images?imageUrl={url}` | 이미지 삭제 | ✅ |

#### 7.1 이미지 업로드
```http
POST /api/images/upload
Authorization: Bearer {token}
Content-Type: application/json

{
    "imageData": "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAA...",
    "dreamId": 1
}
```

**Response (200 OK):**
```json
{
    "success": true,
    "message": "이미지가 성공적으로 업로드되었습니다.",
    "imageUrl": "/uploads/images/dream/1/20251208_abc123.png"
}
```

---

### 에러 응답 형식

```json
{
    "error": "ERROR_CODE",
    "message": "에러 메시지",
    "timestamp": "2025-12-08T10:30:00"
}
```

| HTTP Status | Error Code | 설명 |
|-------------|------------|------|
| 400 | BAD_REQUEST | 잘못된 요청 |
| 400 | VALIDATION_ERROR | 유효성 검증 실패 |
| 401 | UNAUTHORIZED | 인증 실패 |
| 403 | FORBIDDEN | 접근 권한 없음 |
| 404 | NOT_FOUND | 리소스 없음 |
| 409 | CONFLICT | 중복 데이터 |
| 503 | SERVICE_UNAVAILABLE | 외부 서비스 오류 |

---

## ⚙ 환경 설정

### application.yaml

```yaml
# 서버 포트
server:
  port: ${SERVER_PORT:8080}

# 데이터베이스 설정
spring:
  config:
    import: optional:file:.env[.properties]
  datasource:
    driver-class-name: ${SPRING_DATASOURCE_DRIVER_CLASS_NAME:com.mysql.cj.jdbc.Driver}
    url: ${SPRING_DATASOURCE_URL:jdbc:mysql://localhost:3306/dream_db?serverTimezone=Asia/Seoul&characterEncoding=UTF-8}
    username: ${SPRING_DATASOURCE_USERNAME:root}
    password: ${SPRING_DATASOURCE_PASSWORD:ssafy}
    hikari:
      maximum-pool-size: 10
      connection-timeout: 30000

  # 파일 업로드 설정
  servlet:
    multipart:
      max-file-size: 10MB
      max-request-size: 10MB

# MyBatis 설정
mybatis:
  mapper-locations: classpath:mapper/**/*.xml
  type-aliases-package: com.ssafy.finalproject.model.entity
  configuration:
    map-underscore-to-camel-case: true

# JWT 설정
jwt:
  secret: ${JWT_SECRET:your-secret-key-here}
  expiration: ${JWT_EXPIRATION:86400000}  # 24시간

# 파일 업로드 경로
file:
  upload-dir: ${FILE_UPLOAD_DIR:uploads/images}
  base-url: ${FILE_BASE_URL:/uploads/images}

# FastAPI 연동 설정
fastapi:
  url: ${FASTAPI_URL:http://localhost:8000}
  timeout: ${FASTAPI_TIMEOUT:60000}

# 로깅 설정
logging:
  level:
    com.ssafy.finalproject: DEBUG
    org.springframework.web: INFO
    org.springframework.security: DEBUG
```

### 환경 변수 (.env)

```properties
# 서버
SERVER_PORT=8080

# 데이터베이스
SPRING_DATASOURCE_URL=jdbc:mysql://localhost:3306/dream_db?serverTimezone=Asia/Seoul&characterEncoding=UTF-8
SPRING_DATASOURCE_USERNAME=root
SPRING_DATASOURCE_PASSWORD=your_password

# JWT
JWT_SECRET=your-very-long-secret-key-for-jwt-authentication
JWT_EXPIRATION=86400000

# FastAPI
FASTAPI_URL=http://localhost:8000
FASTAPI_TIMEOUT=60000

# 파일 업로드
FILE_UPLOAD_DIR=uploads/images
FILE_BASE_URL=/uploads/images
```

---

## 🚀 실행 방법

### 1. 사전 요구사항

- **Java 17** 이상
- **MySQL 8.0** 이상
- **Gradle 8.x** (또는 Gradle Wrapper 사용)

### 2. 데이터베이스 설정

```bash
# MySQL 접속
mysql -u root -p

# 데이터베이스 및 테이블 생성
source dream_DB.sql
```

### 3. 환경 변수 설정

```bash
# .env 파일 생성 (프로젝트 루트)
cp .env.example .env

# 환경 변수 수정
notepad .env  # Windows
```

### 4. 빌드 및 실행

```bash
# Gradle Wrapper로 빌드
./gradlew build

# 실행
./gradlew bootRun

# 또는 JAR 파일로 실행
java -jar build/libs/finalproject-0.0.1-SNAPSHOT.jar
```

### 5. 접속 확인

```bash
# API 테스트
curl http://localhost:8080/api/emotions

# Swagger UI
http://localhost:8080/swagger-ui/index.html
```

---

## 🎯 주요 기능 상세

### 1. 회원 관리

#### 회원가입 플로우
```
사용자 입력 → 아이디 중복 체크 → 비밀번호 BCrypt 암호화 → DB 저장
```

#### 달력 유형 (calendarType)
| 값 | 설명 |
|----|------|
| `solar` | 양력 |
| `lunarGeneral` | 음력 (평달) |
| `lunarLeap` | 음력 (윤달) |

### 2. 꿈 일기

#### 같은 날짜 처리
- 같은 날짜에 꿈 일기가 있으면 **업데이트**
- 없으면 **새로 생성**

#### Soft Delete
- 실제 삭제 대신 `deleted_date` 컬럼에 삭제 시간 기록
- 조회 시 `deleted_date IS NULL` 조건으로 필터링

### 3. AI 분석 연동

#### FastAPI 연동 구조
```
Spring Boot                    FastAPI (AI Server)
    │                               │
    │  POST /api/v1/fortune/monthly-analysis
    │──────────────────────────────►│
    │  {                            │
    │    user_name: "홍길동",        │
    │    birth_date: "1990-01-15",  │
    │    daily_data: [...]          │
    │  }                            │
    │                               │
    │◄──────────────────────────────│
    │  {                            │
    │    report: "## 월별 분석..."   │
    │  }                            │
    └───────────────────────────────┘
```

### 4. 이미지 처리

#### 저장 구조
```
uploads/
└── images/
    └── dream/
        └── {userId}/
            └── {timestamp}_{uuid}.{ext}
```

#### 지원 형식
- JPEG/JPG
- PNG
- GIF
- WebP

---

## 🔒 보안

### JWT 인증

#### 토큰 구조
```
Header.Payload.Signature

Payload:
{
    "sub": "1",           // userId
    "loginId": "testuser",
    "iat": 1702012800,    // 발급 시간
    "exp": 1702099200     // 만료 시간 (24시간 후)
}
```

#### 인증 헤더
```http
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

### 인증 제외 경로
```java
// 인증 불필요
/api/auth/signup
/api/auth/login
/swagger-ui/**
/v3/api-docs/**
/uploads/**

// 인증 필요
/api/** (나머지 모든 API)
```

### CORS 설정
```java
// 허용된 Origin
http://localhost:3000
http://localhost:5173
https://*.ngrok-free.app
https://*.ngrok.io
```

### 비밀번호 암호화
```java
// BCrypt 사용 (강도 10)
BCryptPasswordEncoder.encode(password)
```

---

## 📖 코드 상세 설명

### 계층 구조

```
┌─────────────────────────────────────────────────────┐
│                   Controller Layer                   │
│  (REST API 엔드포인트, 요청/응답 처리)               │
└────────────────────────┬────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────┐
│                    Service Layer                     │
│  (비즈니스 로직, 트랜잭션 관리)                      │
└────────────────────────┬────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────┐
│                      DAO Layer                       │
│  (MyBatis Mapper Interface)                          │
└────────────────────────┬────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────┐
│                  Mapper XML Layer                    │
│  (SQL 쿼리 정의)                                     │
└─────────────────────────────────────────────────────┘
```

### 주요 클래스 설명

#### 1. SecurityConfig.java
```java
// Spring Security 설정
- JWT 필터 등록
- 인증 제외 경로 설정
- CORS 설정 활성화
- 세션 Stateless 설정
- BCrypt 비밀번호 인코더 Bean 등록
```

#### 2. JwtUtil.java
```java
// JWT 토큰 유틸리티
- generateToken(): 토큰 생성
- getUserIdFromToken(): 토큰에서 userId 추출
- validateToken(): 토큰 유효성 검증
```

#### 3. JwtAuthenticationFilter.java
```java
// JWT 인증 필터
- 모든 요청에서 Authorization 헤더 확인
- Bearer 토큰 추출 및 검증
- SecurityContext에 인증 정보 설정
```

#### 4. GlobalExceptionHandler.java
```java
// 전역 예외 처리
- @RestControllerAdvice 사용
- 커스텀 예외별 HTTP 상태 코드 매핑
- 통일된 에러 응답 형식 반환
```

#### 5. SecurityUtil.java
```java
// 현재 인증된 사용자 정보 추출
- SecurityContextHolder에서 userId 가져오기
- 모든 서비스에서 공통 사용
```

### MyBatis Mapper 패턴

```xml
<!-- ResultMap 정의 -->
<resultMap id="DreamResultMap" type="Dream">
    <id property="dreamId" column="dream_id"/>
    <result property="userId" column="user_id"/>
    ...
</resultMap>

<!-- 조회 쿼리 -->
<select id="findById" resultMap="DreamResultMap">
    SELECT * FROM dreams
    WHERE dream_id = #{dreamId}
      AND deleted_date IS NULL
</select>

<!-- Soft Delete -->
<update id="deleteDream">
    UPDATE dreams
    SET deleted_date = NOW()
    WHERE dream_id = #{dreamId}
</update>
```

---

## 📝 개발 규칙

### 네이밍 컨벤션

| 대상 | 규칙 | 예시 |
|------|------|------|
| 클래스 | PascalCase | `DreamController` |
| 메서드 | camelCase | `getDreamsByMonth` |
| 변수 | camelCase | `dreamCount` |
| 상수 | UPPER_SNAKE_CASE | `MAX_FILE_SIZE` |
| DB 컬럼 | snake_case | `dream_date` |
| API 경로 | kebab-case | `/api/dreams/{id}/result` |

### 응답 형식

#### 성공 응답
```json
{
    "데이터 필드들": "...",
    "message": "성공 메시지"
}
```

#### 에러 응답
```json
{
    "error": "ERROR_CODE",
    "message": "에러 메시지",
    "timestamp": "2025-12-08T10:30:00"
}
```

---

## 📄 라이선스

Apache License 2.0

---

## 👥 팀원

SSAFY Final Project Team

---

<div align="center">

**Made with ❤️ by SSAFY**

</div>

