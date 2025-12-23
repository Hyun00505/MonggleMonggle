package com.ssafy.finalproject.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ssafy.finalproject.model.dto.request.CreateDreamRequest;
import com.ssafy.finalproject.model.dto.request.UpdateDreamRequest;
import com.ssafy.finalproject.model.dto.response.ApiResponse;
import com.ssafy.finalproject.model.dto.response.DreamListResponse;
import com.ssafy.finalproject.model.dto.response.DreamResponse;
import com.ssafy.finalproject.model.dto.response.GalleryResponse;
import com.ssafy.finalproject.service.DreamService;
import com.ssafy.finalproject.util.SecurityUtil;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "02. 꿈 일기 API", description = "꿈 일기 작성, 조회, 수정, 삭제 API")
@RestController
@RequestMapping("/api/dreams")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
public class DreamController {
    
    private final DreamService dreamService;
    
    @Operation(summary = "02-1. 꿈 일기 작성", description = "새로운 꿈 일기를 작성합니다.")
    @PostMapping
    public ResponseEntity<DreamResponse> createDream(@Valid @RequestBody CreateDreamRequest request) {
        Long userId = SecurityUtil.getCurrentUserId();
        DreamResponse response = dreamService.createDream(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @Operation(summary = "02-2. 월별 꿈 일기 목록 조회", description = "특정 월의 꿈 일기 목록을 조회합니다.")
    @GetMapping
    public ResponseEntity<DreamListResponse> getDreamsByMonth(
            @RequestParam Integer year,
            @RequestParam Integer month) {
        Long userId = SecurityUtil.getCurrentUserId();
        DreamListResponse response = dreamService.getDreamsByMonth(userId, year, month);
        return ResponseEntity.ok(response);
    }
    
    @Operation(summary = "02-3. 꿈 일기 상세 조회", description = "특정 꿈 일기의 상세 정보를 조회합니다.")
    @GetMapping("/{dreamId}")
    public ResponseEntity<DreamResponse> getDream(@PathVariable Long dreamId) {
        Long userId = SecurityUtil.getCurrentUserId();
        DreamResponse response = dreamService.getDream(userId, dreamId);
        return ResponseEntity.ok(response);
    }
    
    @Operation(summary = "02-4. 꿈 일기 수정", description = "꿈 일기를 수정합니다.")
    @PutMapping("/{dreamId}")
    public ResponseEntity<ApiResponse> updateDream(
            @PathVariable Long dreamId,
            @Valid @RequestBody UpdateDreamRequest request) {
        Long userId = SecurityUtil.getCurrentUserId();
        dreamService.updateDream(userId, dreamId, request);
        return ResponseEntity.ok(ApiResponse.builder()
                .message("꿈 일기가 수정되었습니다.")
                .build());
    }
    
    @Operation(summary = "02-5. 꿈 일기 삭제", description = "꿈 일기를 삭제합니다 (Soft Delete).")
    @DeleteMapping("/{dreamId}")
    public ResponseEntity<ApiResponse> deleteDream(@PathVariable Long dreamId) {
        Long userId = SecurityUtil.getCurrentUserId();
        dreamService.deleteDream(userId, dreamId);
        return ResponseEntity.ok(ApiResponse.builder()
                .message("꿈 일기가 삭제되었습니다.")
                .build());
    }
    
    @Operation(summary = "02-6. 이미지가 있는 꿈 전체 조회 (갤러리용)", description = "사용자의 이미지가 있는 모든 꿈과 해몽 결과를 조회합니다.")
    @GetMapping("/gallery")
    public ResponseEntity<GalleryResponse> getDreamsWithImages() {
        Long userId = SecurityUtil.getCurrentUserId();
        GalleryResponse response = dreamService.getDreamsWithImages(userId);
        return ResponseEntity.ok(response);
    }
}

