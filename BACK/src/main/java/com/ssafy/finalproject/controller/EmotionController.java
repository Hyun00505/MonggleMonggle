package com.ssafy.finalproject.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ssafy.finalproject.model.dto.response.EmotionListResponse;
import com.ssafy.finalproject.service.EmotionService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "04. 감정 점수 API", description = "감정 점수 목록 조회 API")
@RestController
@RequestMapping("/api/emotions")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
public class EmotionController {
    
    private final EmotionService emotionService;
    
    @Operation(summary = "04-1. 감정 점수 목록 조회", description = "사용 가능한 감정 점수 목록을 조회합니다.")
    @GetMapping
    public ResponseEntity<EmotionListResponse> getAllEmotions() {
        EmotionListResponse response = emotionService.getAllEmotions();
        return ResponseEntity.ok(response);
    }
}

