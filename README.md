<div align="center">

# 🌙 몽글몽글 (MonggleMonggle)

### AI 기반 꿈 일기 & 해몽 서비스

**당신의 꿈을 기록하고, AI가 해석해드립니다**

![Vue.js](https://img.shields.io/badge/Vue.js-3.5-4FC08D?style=for-the-badge&logo=vue.js&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.8-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)
![FastAPI](https://img.shields.io/badge/FastAPI-0.100+-009688?style=for-the-badge&logo=fastapi&logoColor=white)
![Python](https://img.shields.io/badge/Python-3.8+-3776AB?style=for-the-badge&logo=python&logoColor=white)
![Java](https://img.shields.io/badge/Java-17-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![MySQL](https://img.shields.io/badge/MySQL-8.0-4479A1?style=for-the-badge&logo=mysql&logoColor=white)

</div>

---

## 📖 프로젝트 소개

**몽글몽글**은 사용자가 매일의 꿈을 캘린더에 기록하고, AI를 통해 **꿈 해몽**, **오늘의 운세**, **행운의 색상/아이템**을 제공받을 수 있는 서비스입니다. 또한 꿈 내용을 바탕으로 **AI 이미지 생성** 기능과 **월간 분석 리포트** 기능을 제공합니다.

### 🎯 핵심 기능

|        기능        | 설명                                                  |
| :----------------: | :---------------------------------------------------- |
|  **꿈 일기 작성**  | 캘린더 기반으로 매일의 꿈을 기록하고 감정 상태를 표시 |
|   **AI 꿈 해몽**   | 로컬 LLM(Gemma-2-9B)을 활용한 꿈 해석                 |
|  **오늘의 운세**   | 네이버 운세 크롤링 + AI 통합 분석                     |
| **꿈 이미지 생성** | Gemini 2.0 Flash로 꿈을 시각화                        |
|   **월간 분석**    | 한 달간의 꿈 패턴 분석 및 AI 리포트                   |
|  **월간 꿈 편지**  | AI가 작성하는 개인화된 월간 편지                      |

---

## 🖼️ 서비스 화면

### 🔐 로그인 / 회원가입

<p align="center">
  <img src="./README_img/로그인.png" width="45%" alt="로그인">
  <img src="./README_img/회원가입.png" width="45%" alt="회원가입">
</p>

> 몽환적인 밤하늘 배경과 함께 꿈의 여정을 시작하세요

---

### 📅 캘린더 메인

<p align="center">
  <img src="./README_img/캘린더 메인.png" width="80%" alt="캘린더 메인">
</p>

> AI 티켓 시스템으로 하루 사용량 관리 / 날짜별 꿈 기록 확인 / 해몽 결과의 행운 색상 표시

---

### 👤 마이페이지

<p align="center">
  <img src="./README_img/마이페이지.png" width="60%" alt="마이페이지">
</p>

> 개인 정보 관리 / 생년월일 기반 운세 분석

---

### ✍️ 꿈 일기 작성

<p align="center">
  <img src="./README_img/꿈 일기 작성.png" width="80%" alt="꿈 일기 작성">
</p>

> 감정 상태 선택 / 꿈 내용 자유롭게 작성 / AI 해몽 요청

---

### 🔮 AI 해몽 결과

<p align="center">
  <img src="./README_img/꿈 일기 해몽 로딩.png" width="45%" alt="해몽 로딩">
  <img src="./README_img/꿈 해몽 결과.png" width="45%" alt="해몽 결과">
</p>

> 꿈 해몽 & 오늘의 운세 통합 분석 / 행운의 색상 & 아이템 추천 / AI 이미지 생성 (다양한 스타일 선택 가능)

---

### 🖼️ 이미지 갤러리

<p align="center">
  <img src="./README_img/꿈 시각화 갤러리.png" width="80%" alt="이미지 갤러리">
</p>

> AI가 생성한 꿈 이미지들을 모아보는 갤러리 / 태그 및 필터 기능

---

### 📊 월별 분석 & 꿈 편지

<p align="center">
  <img src="./README_img/꿈 월별 분석.png" width="45%" alt="월별 분석">
  <img src="./README_img/월간 꿈 편지.png" width="45%" alt="월간 꿈 편지">
</p>

> 한 달간 기록한 꿈 통계 / AI가 분석한 월간 리포트 / 개인화된 월간 꿈 편지

---
### 🏆 랭킹 시스템

<p align="center">
  <img src="./README_img/" width="45%" alt="랭킹 시스템">
</p>

> 사용자 별 얼만큼 일기를 썼는지에 대한 랭킹 시스템 표시
---
## 🏗️ 시스템 아키텍처

```
┌──────────────────────────────────────────────────────────────────────────┐
│                           몽글몽글 서비스 구조                              │
└──────────────────────────────────────────────────────────────────────────┘

                    ┌─────────────────────────────┐
                    │         사용자 (Web)         │
                    └──────────────┬──────────────┘
                                   │
                    ┌──────────────▼──────────────┐
                    │      FRONTEND (Vue.js)      │
                    │    ───────────────────────  │
                    │  • Vue 3 + Composition API  │
                    │  • Pinia (상태 관리)         │
                    │  • Tailwind CSS             │
                    │  • Vite                     │
                    │    Port: 5173               │
                    └──────────────┬──────────────┘
                                   │ HTTP/REST
                    ┌──────────────▼──────────────┐
                    │     BACKEND (Spring Boot)   │
                    │    ───────────────────────  │
                    │  • Spring Boot 3.5.8        │
                    │  • Spring Security + JWT    │
                    │  • MyBatis + MySQL          │
                    │  • WebClient                │
                    │    Port: 8080               │
                    └────────┬───────────┬────────┘
                             │           │
              ┌──────────────▼───┐   ┌───▼──────────────┐
              │   MySQL 8.0      │   │   AI API Server  │
              │  ─────────────   │   │  ──────────────  │
              │  • 사용자 정보     │   │  • FastAPI       │
              │  • 꿈 일기        │   │  • Gemma-2-9B    │
              │  • 해몽 결과      │   │  • Gemini 2.0    │
              │  • 월간 분석      │   │  • GPT-5-mini    │
              │    Port: 3306    │   │    Port: 8000    │
              └──────────────────┘   └──────────────────┘
```

---

## 🗄️ ERD (데이터베이스 설계)

<p align="center">
  <img src="./README_img/ERD 다이어그램.png" width="90%" alt="ERD 다이어그램">
</p>

> 사용자 정보, 꿈 일기, 해몽 결과, 월간 분석, 공지사항 등의 데이터를 관리하는 관계형 데이터베이스 구조

---

## 🛠️ 기술

### Frontend

|     기술     | 버전 | 설명                    |
| :----------: | :--: | :---------------------- |
|    Vue.js    | 3.5  | 프론트엔드 프레임워크   |
|    Pinia     | 2.2  | 상태 관리 라이브러리    |
|  Vue Router  | 4.6  | SPA 라우팅              |
|    Axios     | 1.13 | HTTP 클라이언트         |
| Tailwind CSS | 4.1  | 유틸리티 CSS 프레임워크 |
|     Vite     | 7.1  | 빌드 도구               |

### Backend

|      기술       |  버전  | 설명                   |
| :-------------: | :----: | :--------------------- |
|   Spring Boot   | 3.5.8  | 백엔드 프레임워크      |
|      Java       |   17   | 프로그래밍 언어        |
| Spring Security |  6.x   | 보안 및 인증           |
|       JWT       | 0.12.3 | 토큰 기반 인증         |
|     MyBatis     | 3.0.3  | ORM 프레임워크         |
|      MySQL      |  8.0   | 관계형 데이터베이스    |
|    WebClient    |   -    | 비동기 HTTP 클라이언트 |

### AI Server

|       기술       |  버전  | 설명                       |
| :--------------: | :----: | :------------------------- |
|     FastAPI      | 0.100+ | Python 웹 프레임워크       |
| llama-cpp-python |   -    | 로컬 LLM 추론 (Gemma-2-9B) |
| Gemini 2.0 Flash |   -    | AI 이미지 생성             |
|    GPT-5-mini    |   -    | 통합 분석 및 JSON 생성     |
|     Selenium     |   -    | 네이버 운세 크롤링         |

---

## 📁 프로젝트 구조

```
MonggleMonggle/
├── 📁 FRONT/                    # 프론트엔드 (Vue.js)
│   ├── src/
│   │   ├── components/          # 재사용 컴포넌트
│   │   ├── views/               # 페이지 컴포넌트
│   │   ├── stores/              # Pinia 스토어
│   │   ├── services/            # API 서비스
│   │   ├── composables/         # Composition API 훅
│   │   ├── router/              # 라우팅 설정
│   │   ├── layouts/             # 레이아웃 컴포넌트
│   │   ├── assets/              # 스타일 파일
│   │   ├── constants/           # 상수 정의
│   │   ├── data/                # 정적 데이터
│   │   └── utils/               # 유틸리티 함수
│   └── package.json
│
├── 📁 BACK/                     # 백엔드 (Spring Boot)
│   ├── src/main/java/com/ssafy/finalproject/
│   │   ├── controller/          # REST API 컨트롤러
│   │   ├── service/             # 비즈니스 로직
│   │   ├── model/               # Entity, DAO, DTO
│   │   ├── security/            # JWT 인증
│   │   ├── config/              # 설정 클래스
│   │   ├── exception/           # 예외 처리
│   │   └── util/                # 유틸리티 클래스
│   └── build.gradle
│
├── 📁 AI_API/                   # AI 서버 (FastAPI)
│   ├── services/                # AI 서비스 로직
│   │   ├── dream_interprinter_service.py
│   │   ├── image_service.py
│   │   ├── monthly_analysis_service.py
│   │   ├── comprehensive_service.py
│   │   ├── Naver_fortune_api.py # 네이버 운세 크롤링
│   │   └── schemas.py           # 데이터 스키마
│   ├── docs/                    # API 문서
│   ├── main.py                  # FastAPI 앱
│   └── requirements.txt
│
└── 📁 README_img/               # README 이미지
```

---

## 🚀 실행 방법

### 사전 요구사항

- Node.js 20.x 이상
- Java 17 이상
- Python 3.8 이상
- MySQL 8.0 이상
- (주의) 파인튜닝된 모델 깃랩에 없음!

### 1. 데이터베이스 설정

```bash
# MySQL 접속 후 데이터베이스 생성
mysql -u root -p
source BACK/dream_DB.sql
```

### 2. Backend 실행

```bash
cd BACK
# 환경 변수 설정 (.env 파일 생성)
./gradlew bootRun
# http://localhost:8080
```

### 3. AI Server 실행

```bash
cd AI_API
pip install -r requirements.txt
# 모델 파일 다운로드 (models/ 디렉토리)
python main.py
# http://localhost:8000
```

### 4. Frontend 실행

```bash
cd FRONT
npm install
npm run dev
# http://localhost:5173
```

---


## 파일분리

|     역할     | 담당                                             |
| :----------: | :----------------------------------------------- |
| **Frontend** | Vue.js 기반 UI/UX 개발, 상태 관리, API 연동      |
| **Backend**  | Spring Boot REST API, 인증/인가, DB 설계         |
|    **AI**    | FastAPI 서버, LLM 통합, 이미지 생성, 운세 크롤링 |

<div align="center">

### 🌙 "꿈을 기록하고, AI와 함께 해석하세요"

**SSAFY Final Project - 몽글몽글**

</div>
