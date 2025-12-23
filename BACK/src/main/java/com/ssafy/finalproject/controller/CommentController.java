package com.ssafy.finalproject.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ssafy.finalproject.model.dto.request.CreateCommentRequest;
import com.ssafy.finalproject.model.dto.response.ApiResponse;
import com.ssafy.finalproject.model.dto.response.CommentResponse;
import com.ssafy.finalproject.service.CommentService;
import com.ssafy.finalproject.util.SecurityUtil;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "08. 공지사항 댓글 API", description = "공지사항 댓글 작성, 조회, 삭제 API")
@RestController
@RequestMapping("/api/notices/comments")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
public class CommentController {
    
    private final CommentService commentService;
    
    @Operation(summary = "08-1. 댓글 작성", description = "공지사항에 댓글을 작성합니다.")
    @PostMapping
    public ResponseEntity<CommentResponse> createComment(@Valid @RequestBody CreateCommentRequest request) {
        Long userId = SecurityUtil.getCurrentUserId();
        CommentResponse response = commentService.createComment(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @Operation(summary = "08-2. 공지사항별 댓글 목록 조회", description = "특정 공지사항의 모든 댓글을 조회합니다.")
    @GetMapping("/{noticeId}")
    public ResponseEntity<CommentResponse.CommentListResponse> getCommentsByNoticeId(@PathVariable Long noticeId) {
        Long userId = SecurityUtil.getCurrentUserId();
        CommentResponse.CommentListResponse response = commentService.getCommentsByNoticeId(noticeId, userId);
        return ResponseEntity.ok(response);
    }
    
    @Operation(summary = "08-3. 댓글 삭제", description = "댓글을 삭제합니다. 본인 또는 관리자만 삭제할 수 있습니다.")
    @DeleteMapping("/{commentId}")
    public ResponseEntity<ApiResponse> deleteComment(@PathVariable Long commentId) {
        Long userId = SecurityUtil.getCurrentUserId();
        commentService.deleteComment(userId, commentId);
        return ResponseEntity.ok(ApiResponse.builder()
                .message("댓글이 삭제되었습니다.")
                .build());
    }
}
