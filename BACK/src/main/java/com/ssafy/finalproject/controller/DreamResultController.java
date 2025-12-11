package com.ssafy.finalproject.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ssafy.finalproject.model.dto.request.SaveDreamResultRequest;
import com.ssafy.finalproject.model.dto.request.UpdateDreamResultRequest;
import com.ssafy.finalproject.model.dto.response.ApiResponse;
import com.ssafy.finalproject.model.dto.response.DreamResultResponse;
import com.ssafy.finalproject.service.DreamResultService;
import com.ssafy.finalproject.util.SecurityUtil;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "3. AI 분석 결과 API", description = "꿈 해몽 및 운세 분석 결과 관리 API")
@RestController
@RequestMapping("/api/dreams/{dreamId}/result")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
public class DreamResultController {
    
    private final DreamResultService dreamResultService;
    
    @Operation(summary = "3.1 AI 분석 결과 저장", description = "FastAPI에서 받은 AI 분석 결과를 저장합니다.")
    @PostMapping
    public ResponseEntity<Map<String, Object>> saveDreamResult(
            @PathVariable Long dreamId,
            @Valid @RequestBody SaveDreamResultRequest request) {
        Long userId = SecurityUtil.getCurrentUserId();
        Long resultId = dreamResultService.saveDreamResult(userId, dreamId, request);
        
        Map<String, Object> response = new HashMap<>();
        response.put("resultId", resultId);
        response.put("dreamId", dreamId);
        response.put("message", "AI 분석 결과가 저장되었습니다.");
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @Operation(summary = "3.2 AI 분석 결과 조회", description = "특정 꿈 일기의 AI 분석 결과를 조회합니다.")
    @GetMapping
    public ResponseEntity<DreamResultResponse> getDreamResult(@PathVariable Long dreamId) {
        Long userId = SecurityUtil.getCurrentUserId();
        DreamResultResponse response = dreamResultService.getDreamResult(userId, dreamId);
        return ResponseEntity.ok(response);
    }
    
    @Operation(summary = "3.3 AI 분석 결과 수정", description = "AI 분석 결과를 수정합니다 (재해몽 포함).")
    @PutMapping
    public ResponseEntity<Map<String, Object>> updateDreamResult(
            @PathVariable Long dreamId,
            @RequestBody UpdateDreamResultRequest request) {
        Long userId = SecurityUtil.getCurrentUserId();
        Long resultId = dreamResultService.updateDreamResult(userId, dreamId, request);
        
        Map<String, Object> response = new HashMap<>();
        response.put("resultId", resultId);
        response.put("dreamId", dreamId);
        response.put("message", "AI 분석 결과가 수정되었습니다.");
        
        return ResponseEntity.ok(response);
    }
    
    @Operation(summary = "3.4 AI 분석 결과 삭제", description = "AI 분석 결과를 삭제합니다 (재분석을 위한 초기화).")
    @DeleteMapping
    public ResponseEntity<ApiResponse> deleteDreamResult(@PathVariable Long dreamId) {
        Long userId = SecurityUtil.getCurrentUserId();
        dreamResultService.deleteDreamResult(userId, dreamId);
        return ResponseEntity.ok(ApiResponse.builder()
                .message("AI 분석 결과가 삭제되었습니다.")
                .build());
    }
}

