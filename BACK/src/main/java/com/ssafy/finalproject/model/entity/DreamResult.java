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
public class DreamResult {
    private Long id;
    private Long dreamId;
    private String dreamInterpretation;
    private String todayFortuneSummary;
    private String luckyColorName;
    private Integer luckyColorNumber;
    private String luckyColorReason;
    private String luckyItemName;
    private String luckyItemReason;
    private String imageUrl;
    private LocalDateTime createdDate;
    private LocalDateTime deletedDate;
    private Boolean isLiked; 
}

