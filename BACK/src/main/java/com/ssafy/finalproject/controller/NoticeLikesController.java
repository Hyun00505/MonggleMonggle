package com.ssafy.finalproject.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ssafy.finalproject.model.dto.response.NoticeLikesResponse;
import com.ssafy.finalproject.service.NoticeLikesService;
import com.ssafy.finalproject.util.SecurityUtil;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "09. 공지사항 좋아요 API", description = "공지사항 좋아요 토글 및 조회 API")
@RestController
@RequestMapping("/api/notices/likes")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
public class NoticeLikesController {
    
    private final NoticeLikesService noticeLikesService;
    
    @Operation(summary = "09-1. 좋아요 토글", description = "공지사항에 좋아요를 누르거나 취소합니다.")
    @PostMapping("/{noticeId}")
    public ResponseEntity<NoticeLikesResponse> toggleLike(@PathVariable Long noticeId) {
        Long userId = SecurityUtil.getCurrentUserId();
        NoticeLikesResponse response = noticeLikesService.toggleLike(userId, noticeId);
        return ResponseEntity.ok(response);
    }
    
    @Operation(summary = "09-2. 좋아요 상태 조회", description = "공지사항의 좋아요 수와 현재 사용자의 좋아요 여부를 조회합니다.")
    @GetMapping("/{noticeId}")
    public ResponseEntity<NoticeLikesResponse> getLikeStatus(@PathVariable Long noticeId) {
        Long userId = SecurityUtil.getCurrentUserId();
        NoticeLikesResponse response = noticeLikesService.getLikeStatus(userId, noticeId);
        return ResponseEntity.ok(response);
    }
}
