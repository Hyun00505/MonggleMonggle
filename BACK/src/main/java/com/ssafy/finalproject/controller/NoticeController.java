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

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController                    // REST API 컨트롤러임을 표시
@RequestMapping("/api/notices")    // 기본 URL 경로
@RequiredArgsConstructor           // 생성자 주입
public class NoticeController {

    private final NoticeService noticeService;  // Service 주입

    // 전체 조회 
    @GetMapping
    public ResponseEntity<NoticeListResponse> getAllNotices() {
        // 1. Service 호출
        NoticeListResponse response = noticeService.getAllNotices();
        
        // 2. 결과 반환
        return ResponseEntity.ok(response);
    }

    // 상세 조회 
    @GetMapping("/{noticeId}")
    public ResponseEntity<NoticeResponse> getNotice(@PathVariable Long noticeId) {
        //                                          ↑ URL에서 값 추출
        NoticeResponse response = noticeService.getNoticeById(noticeId);
        return ResponseEntity.ok(response);
    }

    // 등록 (ADMIN만 가능)
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<NoticeResponse> createNotice(@Valid @RequestBody CreateNoticeRequest request) {
        Long userId = SecurityUtil.getCurrentUserId();
        NoticeResponse response = noticeService.createNotice(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // 수정 (ADMIN만 가능)
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{noticeId}")
    public ResponseEntity<NoticeResponse> updateNotice(@PathVariable Long noticeId, @Valid @RequestBody UpdateNoticeRequest request) {
        NoticeResponse response = noticeService.updateNotice(noticeId, request);
        return ResponseEntity.ok(response);
    }

    // 삭제 (ADMIN만 가능)
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{noticeId}")
    public ResponseEntity<ApiResponse> deleteNotice(@PathVariable Long noticeId) {
        noticeService.deleteNotice(noticeId);
        return ResponseEntity.ok(ApiResponse.builder()
                .message("공지사항이 삭제되었습니다.")
                .build());
    }
}   