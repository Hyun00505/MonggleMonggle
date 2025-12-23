package com.ssafy.finalproject.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Slf4j
@Service
public class ImageService {

    /**
     * Base64 이미지를 Data URI 형식으로 변환하여 반환 (파일 저장 없음)
     * DB에 직접 Base64 Data URI를 저장하는 방식
     * 
     * @param base64Image Base64 인코딩된 이미지 데이터 (data URI 포함 가능)
     * @param userId 사용자 ID (로깅용)
     * @return Base64 Data URI 문자열
     */
    public String saveBase64Image(String base64Image, Long userId) throws IOException {
        if (base64Image == null || base64Image.isEmpty()) {
            throw new IllegalArgumentException("이미지 데이터가 비어있습니다.");
        }
        
        // 이미 Data URI 형식이면 그대로 반환
        if (base64Image.startsWith("data:image/")) {
            log.info("이미지 Data URI 저장 준비 완료 - userId: {}", userId);
            return base64Image;
        }
        
        // Base64 데이터만 있는 경우 Data URI로 변환
        // 기본 MIME 타입은 PNG로 가정
        String mimeType = "image/png";
        String dataUri = String.format("data:%s;base64,%s", mimeType, base64Image);
        
        log.info("이미지 Data URI 변환 완료 - userId: {}, dataLength: {}", userId, dataUri.length());
        return dataUri;
    }

    /**
     * 이미지 삭제 (Base64 Data URI는 DB에 저장되므로 파일 삭제 불필요)
     * 이 메소드는 호환성을 위해 유지하지만, 실제로는 아무 작업도 수행하지 않음
     * 
     * @param imageUrl 이미지 URL 또는 Data URI
     */
    public void deleteImage(String imageUrl) throws IOException {
        if (imageUrl == null || imageUrl.isEmpty()) {
            return;
        }
        
        // Base64 Data URI인 경우 파일 삭제 불필요
        if (imageUrl.startsWith("data:")) {
            log.debug("Base64 Data URI는 파일 삭제가 필요하지 않습니다.");
            return;
        }
        
        // 기존 파일 경로 형식인 경우도 무시 (더 이상 파일을 저장하지 않음)
        log.debug("이미지 삭제 요청 무시 (Base64 저장 방식 사용 중): {}", 
                imageUrl.length() > 50 ? imageUrl.substring(0, 50) + "..." : imageUrl);
    }
}
