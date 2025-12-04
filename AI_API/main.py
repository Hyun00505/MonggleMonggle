import os
from contextlib import asynccontextmanager

from dotenv import load_dotenv
from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware

# 서비스 모듈 및 스키마 임포트
from services.schemas import (
    ComprehensiveFortuneRequest, 
    ComprehensiveFortuneResponse,
    DreamImageRequest, 
    DreamImageResponse,
    MonthlyAnalysisRequest,
    MonthlyAnalysisResponse
)
from services.dream_interprinter_service import load_llama_model, models
from services.comprehensive_service import process_comprehensive_fortune
from services.image_service import process_dream_image
from services.monthly_analysis_service import process_monthly_analysis

# .env 파일 로드
load_dotenv()

# GMS API 키 설정 (필요 시 확인용, 실제 사용은 서비스 모듈에서)
GMS_API_KEY = os.getenv("GMS_API_KEY", "S14P02AQ06-88cb9f96-1505-4bb8-b068-57fe14afebee")

@asynccontextmanager
async def lifespan(app: FastAPI):
    """
    앱 시작/종료 시 실행되는 수명주기 관리자
    앱 시작 시 무거운 LLM 모델을 미리 로드합니다.
    """
    print("🚀 서버 시작: LLM 모델 로딩 중... (시간이 걸릴 수 있습니다)")
    try:
        # 모델 로드 (dream_interprinter_service의 전역 models 변수에 저장)
        models["llm"] = load_llama_model(
            model_key="q4_k_m",
            n_ctx=2048,
            n_gpu_layers=-1, # GPU 가속 사용
            verbose=True
        )
        print("✅ LLM 모델 로드 완료!")
    except Exception as e:
        print(f"⚠️ 모델 로드 실패: {e}")
        print("꿈 해몽 기능이 제한될 수 있습니다.")
    
    yield
    
    # 앱 종료 시 정리
    models.clear()
    print("👋 서버 종료")

app = FastAPI(
    title="AI 통합 운세 서비스",
    description="꿈 해몽과 네이버 운세를 결합한 통합 운세 API",
    version="1.0.0",
    lifespan=lifespan
)

# CORS 설정 - 프론트엔드 및 백엔드 연동 허용
app.add_middleware(
    CORSMiddleware,
    allow_origins=[
        "http://localhost:5173",  # Vue 개발 서버
        "http://localhost:3000",  # 대체 프론트엔드 포트
        "http://localhost:8080",  # Spring Boot 백엔드
    ],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# --- API 엔드포인트 ---

@app.post("/api/v1/fortune/comprehensive", response_model=ComprehensiveFortuneResponse)
async def get_comprehensive_fortune(request: ComprehensiveFortuneRequest):
    """
    통합 운세 조회 API
    1. 로컬 LLM으로 꿈 해몽
    2. 네이버 운세 크롤링
    3. GMS API로 종합 분석 및 행운의 아이템 추천
    """
    return await process_comprehensive_fortune(request)

@app.post("/api/v1/dream/generate-image", response_model=DreamImageResponse)
async def generate_dream_image(request: DreamImageRequest):
    """
    꿈 내용을 기반으로 이미지를 생성하는 API
    Gemini 2.0 Flash Exp Image Generation 모델 사용
    """
    return await process_dream_image(request)

@app.post("/api/v1/fortune/monthly-analysis", response_model=MonthlyAnalysisResponse)
async def get_monthly_analysis(request: MonthlyAnalysisRequest):
    """
    월간 운세 분석 API
    한 달 동안의 꿈과 운세 데이터를 종합 분석하여 감동적인 리포트 생성
    
    - 31일간의 꿈과 운세 데이터 분석
    - 감정 흐름 및 패턴 분석
    - 심리적 통찰 제공
    - 다음 달을 위한 조언 제공
    """
    return await process_monthly_analysis(request)

# 서버 실행 (개발용)
if __name__ == "__main__":
    import uvicorn
    uvicorn.run("main:app", host="0.0.0.0", port=8000, reload=True)
