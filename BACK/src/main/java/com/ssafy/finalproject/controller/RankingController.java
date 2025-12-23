package com.ssafy.finalproject.controller;

import com.ssafy.finalproject.model.dto.response.RankingResponse;
import com.ssafy.finalproject.service.RankingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "13. 랭킹 API", description = "사용자 꿈 일기 랭킹 조회 API")
@RestController
@RequestMapping("/api/ranking")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
public class RankingController {
    
    private final RankingService rankingService;
    
    @Operation(summary = "13-1. 꿈 일기 개수 랭킹 조회", description = "모든 사용자의 꿈 일기 개수 순위를 조회합니다.")
    @GetMapping
    public ResponseEntity<RankingResponse> getDreamCountRanking() {
        RankingResponse response = rankingService.getDreamCountRanking();
        return ResponseEntity.ok(response);
    }
}

