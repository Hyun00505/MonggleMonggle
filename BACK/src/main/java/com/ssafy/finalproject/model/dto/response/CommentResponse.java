package com.ssafy.finalproject.model.dto.response;

import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentResponse {
    
    private Long commentId;
    private Long noticeId;
    private Long userId;
    private String userName;  // 댓글 작성자 이름
    private String content;
    private LocalDateTime createdDate;
    private boolean isOwner;  // 현재 로그인한 사용자가 작성자인지 여부
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CommentListResponse {
        private Long noticeId;
        private int totalCount;
        private List<CommentResponse> comments;
    }
}
