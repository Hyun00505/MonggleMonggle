import os
import sys
import platform
from typing import Optional, List, Union, Dict
from pathlib import Path

try:
    from llama_cpp import Llama
except ImportError:
    print("오류: llama_cpp 라이브러리가 설치되지 않았습니다. 'pip install llama-cpp-python'을 실행하세요.", file=sys.stderr)
    sys.exit(1)

# 모델 경로 설정
GGUF_DIR = Path("./AI_API/models/")
MODEL_FILES = {
    "q4_k_m": GGUF_DIR / "gemma-2-9b-it-dream-q4_k_m.gguf",
}

# 전역 모델 저장소
models = {}

def load_llama_model(
    model_key: str = "q4_k_m",
    n_ctx: int = 4096,
    n_batch: int = 512,
    n_gpu_layers: int = 0,
    seed: Optional[int] = 42,
    verbose: bool = False,
) -> Llama:
    """LLM 모델 로드"""
    if model_key not in MODEL_FILES:
        raise ValueError(f"알 수 없는 모델 키: {model_key}")

    model_path = MODEL_FILES[model_key]
    if not model_path.exists():
        raise FileNotFoundError(f"모델 파일이 존재하지 않습니다: {model_path}")

    use_mlock = platform.system() != "Windows"
    n_threads = os.cpu_count() or 4

    try:
        llm = Llama(
            model_path=str(model_path),
            n_ctx=n_ctx,
            n_batch=n_batch,
            n_threads=n_threads,
            n_gpu_layers=n_gpu_layers,
            use_mlock=use_mlock,
            use_mmap=True,
            logits_all=False,
            seed=seed,
            verbose=verbose,
        )
    except Exception as e:
        raise RuntimeError(f"모델 초기화 중 오류 발생: {e}")

    return llm


def build_messages(
    user_prompt: Union[str, List[Dict[str, str]]],
    system_prompt: str = "",
    history: Union[None, List[Dict[str, str]]] = None,
    include_system: bool = True,
) -> List[Dict[str, str]]:
    """메시지 배열 구성"""
    messages: List[Dict[str, str]] = []
    if include_system and system_prompt:
        messages.append({"role": "system", "content": system_prompt})
    if history:
        messages.extend(history)
    if isinstance(user_prompt, str):
        messages.append({"role": "user", "content": user_prompt})
    else:
        messages.extend(user_prompt)
    return messages


def _merge_system_prompt_into_user(system_prompt: str, messages: List[Dict[str, str]]) -> List[Dict[str, str]]:
    """시스템 프롬프트를 유저 메시지에 병합 (시스템 역할 미지원 모델용)"""
    if not system_prompt:
        return messages
    for msg in messages:
        if msg["role"] == "user":
            msg["content"] = f"[시스템 지시]\n{system_prompt}\n\n{msg['content']}"
            return messages
    messages.append({"role": "user", "content": f"[시스템 지시]\n{system_prompt}"})
    return messages


def generate_response(
    llm: Llama,
    user_prompt: Union[str, List[Dict[str, str]]],
    system_prompt: str = "",
    history: Union[None, List[Dict[str, str]]] = None,
    max_tokens: int = 1024,
    temperature: float = 0.7,
    top_p: float = 0.9,
    repeat_penalty: float = 1.05,
) -> str:
    """LLM 응답 생성"""
    messages = build_messages(user_prompt, system_prompt, history, include_system=True)
    
    try:
        completion = llm.create_chat_completion(
            messages=messages,
            temperature=temperature,
            top_p=top_p,
            max_tokens=max_tokens,
            repeat_penalty=repeat_penalty,
        )
    except ValueError as err:
        if "system role not supported" in str(err).lower():
            fallback_messages = build_messages(user_prompt, system_prompt, history, include_system=False)
            fallback_messages = _merge_system_prompt_into_user(system_prompt, fallback_messages)
            completion = llm.create_chat_completion(
                messages=fallback_messages,
                temperature=temperature,
                top_p=top_p,
                max_tokens=max_tokens,
                repeat_penalty=repeat_penalty,
            )
        else:
            raise err

    return completion["choices"][0]["message"]["content"].strip()
