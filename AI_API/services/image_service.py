import json
import asyncio
import os
import requests
import base64
import uuid
from datetime import datetime
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
    
    # 스타일별 프롬프트 키워드 정의
    STYLE_PROMPTS = {
        "몽환적": "몽환적이고 신비로운 분위기, dreamlike, mystical atmosphere, ethereal",
        "수채화": "부드러운 수채화 스타일, watercolor painting style, soft colors, artistic",
        "애니메이션": "애니메이션 작화 스타일, anime style, vivid colors, 2D rendering",
        "사실적": "고화질 실사 스타일, photorealistic, high quality, cinematic lighting, 4k",
        "판타지": "판타지 아트 스타일, fantasy art, epic, magical, detailed",
        "추상적": "추상적인 예술 스타일, abstract art, unique shapes, modern art"
    }

    # 선택된 스타일에 맞는 키워드 가져오기 (기본값: 몽환적)
    style_keyword = STYLE_PROMPTS.get(request.style, STYLE_PROMPTS["몽환적"])

    # 고정된 프롬프트
    DEFAULT_PROMPT = f"해당 꿈 내용을 읽고 꿈을 영화의 한 장면처럼 이미지를 생성해줘. 스타일은 '{style_keyword}'로 해줘. 이미지는 정사각형으로 만들어줘. 꿈 내용 : "
    
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
        
        # 저장 경로 설정 (./img 폴더가 없으면 생성)
        save_dir = "./img"
        os.makedirs(save_dir, exist_ok=True)
        
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
                
                # 이미지 디코딩 및 로컬 저장
                try:
                    image_data = base64.b64decode(image_data_b64)
                    
                    # 파일명 생성: 타임스탬프_UUID일부.확장자
                    timestamp = datetime.now().strftime("%Y%m%d_%H%M%S")
                    unique_id = str(uuid.uuid4())[:8]
                    # MIME 타입에서 확장자 추출 (예: image/png -> png)
                    ext = mime_type.split('/')[-1] if '/' in mime_type else 'png'
                    
                    filename = f"{timestamp}_{unique_id}.{ext}"
                    filepath = os.path.join(save_dir, filename)
                    
                    with open(filepath, "wb") as f:
                        f.write(image_data)
                    
                    print(f"[이미지 저장 완료]: {filepath}")
                    
                except Exception as e:
                    print(f"⚠️ 이미지 저장 실패: {e}")
                
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
