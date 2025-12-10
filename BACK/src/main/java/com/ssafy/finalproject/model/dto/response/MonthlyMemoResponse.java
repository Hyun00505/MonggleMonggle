package com.ssafy.finalproject.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MonthlyMemoResponse {
    private Long memoId;
    private Integer year;
    private Integer month;
    private String memoContent;
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;
}

