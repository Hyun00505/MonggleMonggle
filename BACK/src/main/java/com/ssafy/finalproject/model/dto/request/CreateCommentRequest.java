package com.ssafy.finalproject.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateCommentRequest {
    
    @NotNull(message = "공지사항 ID는 필수입니다.")
    private Long noticeId;
    
    @NotBlank(message = "댓글 내용은 필수입니다.")
    private String content;
}
