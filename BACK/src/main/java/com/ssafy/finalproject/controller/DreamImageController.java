package com.ssafy.finalproject.controller;

import com.ssafy.finalproject.model.dto.fastapi.DreamImageGenerationRequestDto;
import com.ssafy.finalproject.model.dto.fastapi.DreamImageGenerationResponseDto;
import com.ssafy.finalproject.service.DreamImageService;
import com.ssafy.finalproject.util.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 꿈 이미지 생성 API
 */
@Slf4j
@RestController
@RequestMapping("/api/dream-images")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
@Tag(name = "11. 꿈 이미지 생성 API", description = "AI 꿈 이미지 생성 API (코인 차감 포함)")
public class DreamImageController {
    
    private final DreamImageService dreamImageService;
    
    @Operation(
            summary = "11-1. 꿈 이미지 생성", 
            description = "꿈 내용을 바탕으로 AI 이미지를 생성합니다. 코인 2개가 차감됩니다."
    )
    @PostMapping("/generate")
    public ResponseEntity<DreamImageGenerationResponseDto> generateDreamImage(
            @RequestBody DreamImageGenerationRequestDto request) {
        
        Long userId = SecurityUtil.getCurrentUserId();
        
        log.info("=== 이미지 생성 요청 받음: userId={}, style={} ===", userId, request.getStyle());
        
        DreamImageGenerationResponseDto response = dreamImageService.generateDreamImage(userId, request);
        
        return ResponseEntity.ok(response);
    }
}
