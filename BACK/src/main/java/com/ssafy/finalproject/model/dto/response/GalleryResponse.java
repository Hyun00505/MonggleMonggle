package com.ssafy.finalproject.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GalleryResponse {
    private List<GalleryItem> items;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GalleryItem {
        // 꿈 정보
        private Long dreamId;
        private LocalDate dreamDate;
        private String title;
        private String content;
        private Integer emotionId;
        
        // 해몽 결과 정보
        private Long resultId;
        private String dreamInterpretation;
        private String todayFortuneSummary;
        private LuckyColorDto luckyColor;
        private LuckyItemDto luckyItem;
        private String imageUrl;
        private LocalDateTime createdDate;
        private Boolean isLiked; 
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LuckyColorDto {
        private String name;
        private Integer number;
        private String reason;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LuckyItemDto {
        private String name;
        private String reason;
    }
}
