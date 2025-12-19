import json
import asyncio
import os
import requests
from pathlib import Path
from dotenv import load_dotenv
from fastapi import HTTPException
from services.schemas import DreamImageRequest, DreamImageResponse, GeneratedImage

# .env 파일에서 환경변수 로드 (현재 파일 기준 상위 폴더의 .env)
ENV_PATH = Path(__file__).resolve().parent.parent / ".env"
load_dotenv(ENV_PATH, override=True)

# GMS API 키 설정 (환경변수에서만 참조)
GMS_API_KEY = os.getenv("GMS_API_KEY")

async def process_dream_image(request: DreamImageRequest) -> DreamImageResponse:
    """
    꿈 내용을 기반으로 이미지를 생성하는 비즈니스 로직
    Gemini 2.0 Flash Exp Image Generation 모델 사용
    """
    
    # 스타일별 프롬프트 키워드 정의 (프론트엔드에서 사용하는 4가지 스타일)
    STYLE_PROMPTS = {
        "몽환적": "몽환적이고 신비로운 분위기, dreamlike, mystical atmosphere, ethereal, soft glow",
        "수채화": "부드러운 수채화 스타일, watercolor painting style, soft colors, artistic, delicate brushstrokes",
        "애니메이션": "애니메이션 작화 스타일, anime style, vivid colors, 2D rendering, Japanese animation",
        "판타지": "판타지 아트 스타일, fantasy art, epic, magical, detailed, enchanted atmosphere",
    }

    # 선택된 스타일에 맞는 키워드 가져오기 (기본값: 몽환적)
    style_keyword = STYLE_PROMPTS.get(request.style, STYLE_PROMPTS["몽환적"])

    # 고정된 프롬프트 (텍스트/워터마크 금지 - 강화 버전)
    DEFAULT_PROMPT = (
        "Generate a purely visual image with ABSOLUTELY NO TEXT, NO LETTERS, NO WORDS, NO NUMBERS, NO SYMBOLS, NO WATERMARKS, NO CAPTIONS, NO SUBTITLES, NO SIGNATURES, NO LOGOS anywhere in the image. "
        "This is extremely important: the image must contain ZERO text elements of any kind. "
        "Visualize the following dream content as a cinematic scene. "
        f"Style: {style_keyword}. Square aspect ratio. "
        "Dream content: "
    )
    
    # 최종 프롬프트 조합
    full_prompt = f"{DEFAULT_PROMPT}{request.dream_prompt}"
    
    print(f"🎨 이미지 생성 요청 중... (Prompt: {request.dream_prompt}, Style: {request.style})")
    
    # GMS 프록시 엔드포인트 URL 설정
    url = f"https://gms.ssafy.io/gmsapi/generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash-exp-image-generation:generateContent?key={GMS_API_KEY}"
    
    headers = {
        "Content-Type": "application/json"
    }
    
    payload = {
        "contents": [
            {
                "parts": [
                    {
                        "text": full_prompt
                    }
                ]
            }
        ],
        "generationConfig": {
            "responseModalities": ["TEXT", "IMAGE"]
        }
    }
    
    try:
        # 비동기로 HTTP 요청 보내기
        loop = asyncio.get_event_loop()
        response = await loop.run_in_executor(
            None, 
            lambda: requests.post(url, headers=headers, data=json.dumps(payload))
        )
        
        # 응답 확인
        if response.status_code != 200:
            raise HTTPException(
                status_code=response.status_code,
                detail=f"이미지 생성 실패: {response.text}"
            )
        
        response_data = response.json()
        
        # 응답 데이터 구조 파싱
        candidates = response_data.get('candidates', [])
        if not candidates:
            return DreamImageResponse(
                success=False,
                message="생성된 결과(candidates)가 없습니다.",
                images=[]
            )
        
        # 첫 번째 후보의 내용 가져오기
        parts = candidates[0].get('content', {}).get('parts', [])
        
        generated_images = []
        model_text = None
        
        # parts 리스트를 순회하며 텍스트와 이미지를 분리 처리
        for part in parts:
            # 텍스트가 있는 경우 저장
            if 'text' in part:
                model_text = part['text']
                print(f"[모델 응답 텍스트]: {model_text}")
            
            # 이미지가 있는 경우 Base64 데이터 추가
            if 'inlineData' in part:
                mime_type = part['inlineData'].get('mimeType', 'image/png')
                image_data_b64 = part['inlineData']['data']
                
                # Base64 데이터만 응답에 추가 (파일 저장은 Spring Boot에서 처리)
                generated_images.append(GeneratedImage(
                    image_data=image_data_b64,
                    mime_type=mime_type
                ))
                print(f"[이미지 생성 완료]: {mime_type}")
        
        if not generated_images:
            return DreamImageResponse(
                success=True,
                message="이미지가 생성되지 않았습니다. (텍스트만 반환됨)",
                images=[],
                model_text=model_text
            )
        
        return DreamImageResponse(
            success=True,
            message=f"{len(generated_images)}개의 이미지가 생성되었습니다.",
            images=generated_images,
            model_text=model_text
        )
        
    except HTTPException:
        raise
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"이미지 생성 중 오류 발생: {str(e)}")
