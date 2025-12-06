import json
import os
import asyncio
import requests
from pathlib import Path
from dotenv import load_dotenv
from fastapi import HTTPException
from typing import List
from services.schemas import MonthlyAnalysisRequest, MonthlyAnalysisResponse

# .env 파일에서 환경변수 로드 (현재 파일 기준 상위 폴더의 .env)
ENV_PATH = Path(__file__).resolve().parent.parent / ".env"
load_dotenv(ENV_PATH, override=True)

# GMS API 키 설정 (환경변수에서만 참조)
GMS_API_KEY = os.getenv("GMS_API_KEY")

def create_compact_monthly_prompt(request: MonthlyAnalysisRequest) -> tuple[str, dict]:
    """
    31일 전체를 압축된 형태로 프롬프트에 포함
    
    Args:
        request: 월간 분석 요청 데이터
        
    Returns:
        tuple: (프롬프트 문자열, 통계 정보 딕셔너리)
    """
    
    all_data = [data.model_dump() for data in request.daily_data]
    
    # 통계 계산
    emotion_scores = [d['emotion_score'] for d in all_data]
    stats = {
        'total_days': len(all_data),
        'avg_emotion': round(sum(emotion_scores) / len(emotion_scores), 2),
        'emotion_distribution': {i: emotion_scores.count(i) for i in range(1, 6)},
        'period': all_data[0]['date'][:7],
        'user_name': request.user_name,
        'birth_date': request.birth_date
    }
    
    emotion_labels = {
        1: "매우 부정적", 2: "부정적", 3: "중립", 4: "긍정적", 5: "매우 긍정적"
    }
    
    prompt = f"""당신은 꿈 분석 전문가입니다. {stats['user_name']}님({stats['birth_date']}생)의 {stats['period']} 한 달 꿈 일기를 분석해주세요.

## 통계
- 총 {stats['total_days']}일 기록
- 평균 감정: {stats['avg_emotion']}/5.0
- 분포: 😊{stats['emotion_distribution'].get(5, 0)}일 🙂{stats['emotion_distribution'].get(4, 0)}일 😐{stats['emotion_distribution'].get(3, 0)}일 😟{stats['emotion_distribution'].get(2, 0)}일 😢{stats['emotion_distribution'].get(1, 0)}일

## 날짜별 운세 분석 (전체 {stats['total_days']}일)

"""
    
    # 모든 데이터를 간결하게 포함
    for data in all_data:
        # today_fortune_summary의 핵심만 추출 (첫 2문장 또는 200자)
        summary = data['today_fortune_summary']
        # 마침표나 느낌표 기준으로 앞 2문장만
        sentences = summary.replace('다. ', '다.|').replace('니다. ', '니다.|').split('|')
        compact_summary = sentences[0] if sentences else summary[:200]
        
        # 꿈 내용도 간략하게 (첫 100자)
        dream_content = data['dream_content']
        compact_dream = dream_content[:100] + "..." if len(dream_content) > 100 else dream_content
        
        prompt += f"**[{data['date']}] 감정{data['emotion_score']}점** - {compact_dream}\n"
        prompt += f"{compact_summary}\n\n"
    
    prompt += f"""
## 월간 리포트 작성 요청

위 31일 전체의 꿈과 운세를 종합 분석하여, 감동적이고 서정적인 월간 리포트를 작성해주세요.

### 구성
1. **인사말** (50자): 해당 월의 날씨/계절 특징을 은유적으로
2. **이번 달의 감정 여정** (300-400자): 초중후반의 감정 흐름을 이야기체로
3. **꿈이 전하는 메시지** (400-500자): 반복 주제, 패턴, 구체적 날짜 언급
4. **심리적 통찰과 성장** (300-400자): 내면의 변화와 성장
5. **다음 달을 위한 조언** (200-250자): 따뜻하고 실천 가능한 격려

### 작성 가이드
- 사용자 이름을 자연스럽게 사용
- 감동적이고 개인화된 느낌
- 심리학적 깊이 + 문학적 아름다움
- 통계 수치는 자연스럽게 녹여서
- 각 섹션 구분은 명확하되 흐름은 자연스럽게하고 사용자에겐 목차를 보여주지 마세요

"""

    return prompt, stats


async def generate_monthly_report_with_gms(prompt: str, stats: dict) -> str:
    """
    GMS API를 사용하여 월간 리포트 생성
    
    Args:
        prompt: 생성할 프롬프트
        stats: 통계 정보
        
    Returns:
        str: 생성된 월간 리포트
    """
    
    print(f"🌙 월간 꿈 분석 리포트 생성 시작")
    print(f"   - 프롬프트 길이: {len(prompt):,}자")
    print(f"   - 예상 토큰: ~{len(prompt)//4:,} tokens")
    print(f"   - 기간: {stats['period']}")
    print(f"   - 사용자: {stats['user_name']}님")
    
    try:
        url = "https://gms.ssafy.io/gmsapi/api.openai.com/v1/chat/completions"
        
        headers = {
            "Content-Type": "application/json",
            "Authorization": f"Bearer {GMS_API_KEY}"
        }
        
        system_prompt = "당신은 꿈과 심리 분석 전문가입니다. 감동적이고 서정적인 글을 작성합니다."
        
        payload = {
            "model": "gpt-5-mini",
            "messages": [
                {"role": "developer", "content": system_prompt},
                {"role": "user", "content": prompt}
            ]
        }
        
        loop = asyncio.get_event_loop()
        response = await loop.run_in_executor(
            None,
            lambda: requests.post(url, headers=headers, json=payload, timeout=60)
        )
        response.raise_for_status()
        
        result_data = response.json()
        monthly_report = result_data['choices'][0]['message']['content']
        
        print("✅ 월간 리포트 생성 완료!")
        
        return monthly_report
        
    except requests.exceptions.Timeout:
        raise HTTPException(status_code=504, detail="GMS API 요청 시간 초과")
    except requests.exceptions.RequestException as e:
        print(f"❌ API 요청 오류: {str(e)}")
        # 에러 응답 상세 내용 출력
        try:
            if 'response' in locals():
                print(f"📋 응답 상태 코드: {response.status_code}")
                print(f"📋 응답 내용: {response.text}")
                print(f"📋 요청 payload: {json.dumps(payload, ensure_ascii=False, indent=2)}")
        except:
            pass
        raise HTTPException(status_code=500, detail=f"월간 리포트 생성 실패: {str(e)}")
    except Exception as e:
        print(f"❌ 예상치 못한 오류: {str(e)}")
        raise HTTPException(status_code=500, detail=f"월간 리포트 생성 중 오류 발생: {str(e)}")


async def process_monthly_analysis(request: MonthlyAnalysisRequest) -> MonthlyAnalysisResponse:
    """
    월간 분석 비즈니스 로직
    
    Args:
        request: 월간 분석 요청 데이터
        
    Returns:
        MonthlyAnalysisResponse: 월간 분석 결과
    """
    
    # 데이터 검증
    if not request.daily_data or len(request.daily_data) == 0:
        raise HTTPException(status_code=400, detail="일일 운세 데이터가 비어있습니다.")
    
    print(f"🔍 월간 분석 시작 - {request.user_name}님의 {len(request.daily_data)}일간 데이터")
    
    try:
        # 1. 프롬프트 생성
        prompt, stats = create_compact_monthly_prompt(request)
        
        # 2. GMS API로 월간 리포트 생성
        monthly_report = await generate_monthly_report_with_gms(prompt, stats)
        
        # 3. 결과 반환
        return MonthlyAnalysisResponse(
            period=stats['period'],
            user_name=stats['user_name'],
            report=monthly_report,
            statistics=stats,
            metadata={
                'total_days': stats['total_days'],
                'prompt_length': len(prompt),
                'avg_emotion': stats['avg_emotion']
            }
        )
        
    except HTTPException:
        raise
    except Exception as e:
        print(f"❌ 월간 분석 처리 중 오류: {str(e)}")
        raise HTTPException(status_code=500, detail=f"월간 분석 실패: {str(e)}")

