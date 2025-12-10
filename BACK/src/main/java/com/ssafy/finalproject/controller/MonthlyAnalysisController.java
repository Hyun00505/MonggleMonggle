package com.ssafy.finalproject.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ssafy.finalproject.model.dto.request.MonthlyAnalysisRequest;
import com.ssafy.finalproject.model.dto.response.MonthlyAnalysisResponse;
import com.ssafy.finalproject.service.MonthlyAnalysisService;
import com.ssafy.finalproject.util.SecurityUtil;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "5. 월별 분석 API", description = "월별 꿈 통계 및 AI 리포트 생성 API")
@RestController
@RequestMapping("/api/analysis/monthly")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
public class MonthlyAnalysisController {
    
    private final MonthlyAnalysisService monthlyAnalysisService;
    
    @Operation(summary = "5.0 지난달 리포트 조회/생성", description = "지난달 리포트를 조회하고, 없으면 생성하여 반환합니다.")
    @GetMapping("/auto")
    public ResponseEntity<MonthlyAnalysisResponse> getPreviousMonthAnalysis() {
        Long userId = SecurityUtil.getCurrentUserId();
        MonthlyAnalysisResponse response = monthlyAnalysisService.getOrGeneratePreviousMonthAnalysis(userId);
        return ResponseEntity.ok(response);
    }
    
    @Operation(summary = "5.1 월별 꿈 통계 조회", description = "특정 월의 꿈 통계 및 분석 결과를 조회합니다.")
    @GetMapping
    public ResponseEntity<MonthlyAnalysisResponse> getMonthlyAnalysis(
            @RequestParam Integer year,
            @RequestParam Integer month) {
        Long userId = SecurityUtil.getCurrentUserId();
        MonthlyAnalysisResponse response = monthlyAnalysisService.getMonthlyAnalysis(userId, year, month);
        return ResponseEntity.ok(response);
    }
    
    @Operation(summary = "5.2 월별 AI 리포트 생성", description = "FastAPI를 통해 월별 AI 분석 리포트를 생성합니다.")
    @PostMapping
    public ResponseEntity<MonthlyAnalysisResponse> generateMonthlyReport(
            @Valid @RequestBody MonthlyAnalysisRequest request) {
        Long userId = SecurityUtil.getCurrentUserId();
        MonthlyAnalysisResponse response = monthlyAnalysisService.generateMonthlyReport(
                userId, request.getYear(), request.getMonth());
        
        // 기존 데이터가 업데이트된 경우 200, 새로 생성된 경우 201
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}

