package com.ssafy.finalproject.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NoticeLikesResponse {
    
    private Long noticeId;
    private int likeCount;       // 총 좋아요 수
    private boolean isLiked;     // 현재 사용자가 좋아요를 눌렀는지 여부
}
