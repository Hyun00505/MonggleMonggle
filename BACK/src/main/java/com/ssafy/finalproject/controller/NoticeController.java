package com.ssafy.finalproject.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ssafy.finalproject.model.dto.request.CreateNoticeRequest;
import com.ssafy.finalproject.model.dto.request.UpdateNoticeRequest;
import com.ssafy.finalproject.model.dto.response.ApiResponse;
import com.ssafy.finalproject.model.dto.response.NoticeListResponse;
import com.ssafy.finalproject.model.dto.response.NoticeResponse;
import com.ssafy.finalproject.service.NoticeService;
import com.ssafy.finalproject.util.SecurityUtil;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "07. 공지사항 API", description = "공지사항 작성, 조회, 수정, 삭제 API (관리자 전용 작성/수정/삭제)")
@RestController
@RequestMapping("/api/notices")
@RequiredArgsConstructor
public class NoticeController {

    private final NoticeService noticeService;  // Service 주입

    @Operation(summary = "07-1. 공지사항 전체 조회", description = "모든 공지사항 목록을 조회합니다.")
    @GetMapping
    public ResponseEntity<NoticeListResponse> getAllNotices() {
        // 1. Service 호출
        NoticeListResponse response = noticeService.getAllNotices();
        
        // 2. 결과 반환
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "07-2. 공지사항 상세 조회", description = "특정 공지사항의 상세 내용을 조회합니다.")
    @GetMapping("/{noticeId}")
    public ResponseEntity<NoticeResponse> getNotice(@PathVariable Long noticeId) {
        //                                          ↑ URL에서 값 추출
        NoticeResponse response = noticeService.getNoticeById(noticeId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "07-3. 공지사항 등록 (관리자)", description = "새로운 공지사항을 등록합니다. 관리자만 가능합니다.")
    @SecurityRequirement(name = "Bearer Authentication")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<NoticeResponse> createNotice(@Valid @RequestBody CreateNoticeRequest request) {
        Long userId = SecurityUtil.getCurrentUserId();
        NoticeResponse response = noticeService.createNotice(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "07-4. 공지사항 수정 (관리자)", description = "공지사항을 수정합니다. 관리자만 가능합니다.")
    @SecurityRequirement(name = "Bearer Authentication")
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{noticeId}")
    public ResponseEntity<NoticeResponse> updateNotice(@PathVariable Long noticeId, @Valid @RequestBody UpdateNoticeRequest request) {
        NoticeResponse response = noticeService.updateNotice(noticeId, request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "07-5. 공지사항 삭제 (관리자)", description = "공지사항을 삭제합니다. 관리자만 가능합니다.")
    @SecurityRequirement(name = "Bearer Authentication")
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{noticeId}")
    public ResponseEntity<ApiResponse> deleteNotice(@PathVariable Long noticeId) {
        noticeService.deleteNotice(noticeId);
        return ResponseEntity.ok(ApiResponse.builder()
                .message("공지사항이 삭제되었습니다.")
                .build());
    }
}   