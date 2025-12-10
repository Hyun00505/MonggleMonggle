package com.ssafy.finalproject.model.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateDreamResultRequest {
    
    private String dreamInterpretation;
    
    private String todayFortuneSummary;
    
    private LuckyColorDto luckyColor;
    
    private LuckyItemDto luckyItem;
    
    private String imageUrl;
    
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

