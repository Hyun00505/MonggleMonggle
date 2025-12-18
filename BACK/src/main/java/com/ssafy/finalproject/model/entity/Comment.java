package com.ssafy.finalproject.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Comment {
    private Long commentId;
    private Long noticeId;
    private Long userId;
    private String content;
    private LocalDateTime createdDate;
    private LocalDateTime deletedDate;
}

