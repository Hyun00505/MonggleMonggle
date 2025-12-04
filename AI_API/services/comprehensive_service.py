import json
import asyncio
import datetime
import os
import requests
from fastapi import HTTPException
from services.schemas import ComprehensiveFortuneRequest, ComprehensiveFortuneResponse
from services.dream_interprinter_service import models, generate_response
from services.Naver_fortune_api import fetch_today_fortune

# GMS API 키 설정
GMS_API_KEY = os.getenv("GMS_API_KEY", "S14P02AQ06-88cb9f96-1505-4bb8-b068-57fe14afebee")

async def process_comprehensive_fortune(request: ComprehensiveFortuneRequest) -> ComprehensiveFortuneResponse:
    """
    통합 운세 조회 비즈니스 로직
    1. 로컬 LLM으로 꿈 해몽
    2. 네이버 운세 크롤링
    3. GMS API로 종합 분석 및 행운의 아이템 추천
    """
    
    # 1. 꿈 해몽 (로컬 LLM)
    if "llm" not in models:
        raise HTTPException(status_code=503, detail="LLM 모델이 로드되지 않았습니다.")

    print(f"🔍 [1/3] 꿈 해몽 분석 중... (사용자: {request.name})")
    
    dream_prompt = f"""당신은 한국어로 꿈 해몽을 전문적으로 설명하는 상담가입니다. 
    
[사용자의 꿈 설명]
{request.dream_content}

[요청]
이 꿈이 의미하는 바를 3문장으로 설명해 주세요."""

    try:
        dream_interpretation = generate_response(
            models["llm"],
            user_prompt=dream_prompt,
            max_tokens=512,
            temperature=0.6
        )
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"꿈 해몽 생성 실패: {str(e)}")

    # 2. 네이버 운세 크롤링 (비동기 처리)
    print("🔍 [2/3] 네이버 운세 크롤링 중...")
    # 네이버 API는 소문자 m/f를 사용하므로 변환
    naver_gender = request.gender.lower()
    try:
        naver_fortune = await asyncio.to_thread(
            fetch_today_fortune,
            gender=naver_gender,
            calendar_type=request.calendar_type,
            birth_date=request.birth_date,
            headless=True,
            wait_seconds=10
        )
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"네이버 운세 가져오기 실패: {str(e)}")

    # 3. GMS API로 통합 분석
    print("🔍 [3/3] 통합 운세 리포트 생성 중...")
    
    today_date = datetime.datetime.now().strftime("%Y-%m-%d")
    
    color_options = [
        {"number": 0, "name": "회색"},
        {"number": 1, "name": "갈색"},
        {"number": 2, "name": "주황색"},
        {"number": 3, "name": "노란색"},
        {"number": 4, "name": "초록색"},
        {"number": 5, "name": "파란색"},
        {"number": 6, "name": "보라색"},
        {"number": 7, "name": "분홍색"},
        {"number": 8, "name": "빨간색"}
    ]
    
    system_prompt = (
        "당신은 오늘 하루의 운세를 알려주는 주술가입니다. "
        "사용자의 꿈과 운세를 종합 분석하여 인과관계가 있는 통찰을 제공하세요. "
        "반드시 주어진 JSON 형식으로만 응답해야 합니다."
    )
    
    user_prompt = f"""
### 사용자 정보
- 이름: {request.name}
- 성별: {request.gender}
- 생년월일: {request.birth_date}
- 날짜: {today_date}

### 입력 데이터
1. **꿈 내용:**
{request.dream_content.strip()}

2. **꿈 해몽 분석:**
{dream_interpretation.strip()}

3. **오늘의 운세:**
{naver_fortune.summary.strip()}

4. **선택 가능한 행운의 색상 목록:**
{json.dumps(color_options, ensure_ascii=False)}

### 요청 사항
위 정보를 종합하여 JSON 형식으로 운세 리포트를 작성해주세요.
특히 생년월일과 성별을 고려하여 오늘 사용자에게 행운을 가져다줄 아이템을 추천해주세요.

### 출력 형식 (JSON Only)
{{
  "dream_interpretation": "꿈 해몽 내용 요약...",
  "today_fortune_summary": "운세 내용 작성...",
  "lucky_color": {{
      "name": "색상명 (목록에 있는 것만)",
      "number": 색상 번호 (0-8),
      "reason": "선정 이유 작성, 사용자의 생일은 이유가 될 수 없음."
  }},
  "lucky_item": {{
      "name": "추천 아이템 명",
      "reason": "나이와 성별 기반 추천 이유, 하지만 나이와 성별은 명시하지 말 것"
  }}
}}
"""

    try:
        url = "https://gms.ssafy.io/gmsapi/api.openai.com/v1/chat/completions"
        
        headers = {
            "Content-Type": "application/json",
            "Authorization": f"Bearer {GMS_API_KEY}"
        }
        
        payload = {
            "model": "gpt-5-mini",
            "messages": [
                {"role": "developer", "content": system_prompt},
                {"role": "user", "content": user_prompt}
            ],
            "response_format": {"type": "json_object"}
        }
        
        loop = asyncio.get_event_loop()
        response = await loop.run_in_executor(
            None,
            lambda: requests.post(url, headers=headers, json=payload)
        )
        response.raise_for_status()
        
        result_data = response.json()
        content = result_data['choices'][0]['message']['content']
        
        clean_content = content.replace('```json', '').replace('```', '').strip()
        result_json = json.loads(clean_content)
        
        return ComprehensiveFortuneResponse(
            dream_interpretation=result_json.get("dream_interpretation", dream_interpretation),
            today_fortune_summary=result_json.get("today_fortune_summary", ""),
            lucky_color=result_json.get("lucky_color", {"name": "회색", "number": 0, "reason": "분석 불가"}),
            lucky_item=result_json.get("lucky_item", {"name": "추천 아이템 없음", "reason": "분석 불가"})
        )
        
    except Exception as e:
        print(f"상세 에러: {e}")
        try:
            if 'response' in locals():
                print(f"응답 내용: {response.text}")
        except:
            pass
        raise HTTPException(status_code=500, detail=f"통합 분석 실패: {str(e)}")

