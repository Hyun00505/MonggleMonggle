package com.ssafy.finalproject.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ssafy.finalproject.model.dto.request.SaveMemoRequest;
import com.ssafy.finalproject.model.dto.response.ApiResponse;
import com.ssafy.finalproject.model.dto.response.MonthlyMemoResponse;
import com.ssafy.finalproject.service.MonthlyMemoService;
import com.ssafy.finalproject.util.SecurityUtil;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "06. 월별 메모 API", description = "월별 메모 작성, 조회, 삭제 API")
@RestController
@RequestMapping("/api/memo/monthly")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
public class MonthlyMemoController {
    
    private final MonthlyMemoService monthlyMemoService;
    
    @Operation(summary = "06-1. 월별 메모 조회", description = "특정 월의 메모를 조회합니다.")
    @GetMapping
    public ResponseEntity<java.util.List<MonthlyMemoResponse>> getMemo(
            @RequestParam Integer year,
            @RequestParam Integer month) {
        Long userId = SecurityUtil.getCurrentUserId();
        var response = monthlyMemoService.getMemo(userId, year, month);
        return ResponseEntity.ok(response);
    }
    
    @Operation(summary = "06-2. 월별 메모 작성/수정", description = "월별 메모를 작성하거나 수정합니다.")
    @PostMapping
    public ResponseEntity<Map<String, Object>> saveMemo(@Valid @RequestBody SaveMemoRequest request) {
        Long userId = SecurityUtil.getCurrentUserId();
        Long memoId = monthlyMemoService.saveMemo(userId, request);
        
        Map<String, Object> response = new HashMap<>();
        response.put("memoId", memoId);
        response.put("message", "메모가 저장되었습니다.");
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @Operation(summary = "06-3. 월별 메모 삭제", description = "월별 메모를 삭제합니다.")
    @DeleteMapping("/{memoId}")
    public ResponseEntity<ApiResponse> deleteMemo(@PathVariable Long memoId) {
        Long userId = SecurityUtil.getCurrentUserId();
        monthlyMemoService.deleteMemo(userId, memoId);
        return ResponseEntity.ok(ApiResponse.builder()
                .message("메모가 삭제되었습니다.")
                .build());
    }
}

