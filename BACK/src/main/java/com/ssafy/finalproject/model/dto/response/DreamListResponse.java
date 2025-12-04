package com.ssafy.finalproject.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DreamListResponse {
    private Integer year;
    private Integer month;
    private List<DreamSummary> dreams;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DreamSummary {
        private Long dreamId;
        private String title;
        private String content;
        private String dreamDate;
        private Integer emotionId;
        private String emotionName;
        private Boolean hasResult;
    }
}

