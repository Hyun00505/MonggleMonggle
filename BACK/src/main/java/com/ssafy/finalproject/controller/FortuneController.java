package com.ssafy.finalproject.controller;

import com.ssafy.finalproject.model.dto.fastapi.ComprehensiveFortuneRequestDto;
import com.ssafy.finalproject.model.dto.fastapi.ComprehensiveFortuneResponseDto;
import com.ssafy.finalproject.service.FortuneService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 통합 운세 API 컨트롤러
 * FastAPI의 AI 운세 서비스를 프록시하여 프론트엔드에 제공
 */
@Slf4j
@RestController
@RequestMapping("/api/fortune")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
@Tag(name = "10. 통합 운세 API", description = "꿈 해몽 + 오늘의 운세 통합 분석 API")
public class FortuneController {
    
    private final FortuneService fortuneService;
    
    @Operation(
            summary = "10-1. 통합 운세 조회",
            description = "꿈 내용과 사용자 정보를 바탕으로 꿈 해몽과 오늘의 운세를 통합 분석합니다. " +
                    "AI 처리로 인해 응답 시간이 길어질 수 있습니다 (최대 2분)."
    )
    @PostMapping("/comprehensive")
    public ResponseEntity<ComprehensiveFortuneResponseDto> getComprehensiveFortune(
            @Valid @RequestBody ComprehensiveFortuneRequestDto request) {
        
        log.info("=== 통합 운세 API 요청 받음 ===");
        
        ComprehensiveFortuneResponseDto response = fortuneService.getComprehensiveFortune(request);
        
        return ResponseEntity.ok(response);
    }
}

