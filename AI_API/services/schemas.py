from typing import List, Optional
from pydantic import BaseModel, Field

class ComprehensiveFortuneRequest(BaseModel):
    name: str = Field(..., description="사용자 이름")
    dream_content: str = Field(..., description="꿈 내용")
    gender: str = Field(..., pattern="^(M|F)$", description="'M'(남성) 또는 'F'(여성)")
    calendar_type: str = Field(..., pattern="^(solar|lunarGeneral|lunarLeap)$", description="양력/음력 구분")
    birth_date: str = Field(..., pattern=r"^\d{4}-\d{2}-\d{2}$", description="생년월일 (YYYY-MM-DD)")

class LuckyColor(BaseModel):
    name: str
    number: int
    reason: str

class LuckyItem(BaseModel):
    name: str
    reason: str

class ComprehensiveFortuneResponse(BaseModel):
    dream_interpretation: str
    today_fortune_summary: str
    lucky_color: LuckyColor
    lucky_item: LuckyItem

class DreamImageRequest(BaseModel):
    dream_prompt: str = Field(..., description="꿈 내용 프롬프트 (예: '똥통에 빠지는 꿈을 꿨어')")
    style: str = Field("몽환적", description="이미지 스타일 (몽환적, 수채화, 애니메이션, 사실적, 판타지, 추상적)")

class GeneratedImage(BaseModel):
    image_data: str = Field(..., description="Base64로 인코딩된 이미지 데이터")
    mime_type: str = Field(..., description="이미지 MIME 타입 (예: image/png)")

class DreamImageResponse(BaseModel):
    success: bool
    message: str
    images: List[GeneratedImage] = []
    model_text: Optional[str] = None

class DailyFortuneData(BaseModel):
    date: str = Field(..., pattern=r"^\d{4}-\d{2}-\d{2}$", description="날짜 (YYYY-MM-DD)")
    dream_content: str = Field(..., description="꿈 내용")
    today_fortune_summary: str = Field(..., description="오늘의 운세 요약")
    emotion_score: int = Field(..., ge=1, le=5, description="감정 점수 (1-5)")

class MonthlyAnalysisRequest(BaseModel):
    user_name: str = Field(..., description="사용자 이름")
    birth_date: str = Field(..., pattern=r"^\d{4}-\d{2}-\d{2}$", description="생년월일 (YYYY-MM-DD)")
    daily_data: List[DailyFortuneData] = Field(..., description="31일간의 운세 데이터")

class MonthlyAnalysisResponse(BaseModel):
    period: str = Field(..., description="분석 기간 (YYYY-MM)")
    user_name: str = Field(..., description="사용자 이름")
    report: str = Field(..., description="월간 분석 리포트 전문")
    statistics: dict = Field(..., description="통계 정보")
    metadata: dict = Field(..., description="메타데이터")

