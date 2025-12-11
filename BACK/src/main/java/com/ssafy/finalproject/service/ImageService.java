package com.ssafy.finalproject.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.UUID;

@Slf4j
@Service
public class ImageService {

    // BACK 폴더 내 uploads/images를 기본값으로 사용
    @Value("${file.upload-dir:uploads/images}")
    private String uploadDir;

    @Value("${file.base-url:/uploads/images}")
    private String baseUrl;

    /**
     * Base64 이미지를 파일로 저장하고 URL 반환
     * @param base64Image Base64 인코딩된 이미지 데이터 (data URI 포함 가능)
     * @param userId 사용자 ID
     * @return 저장된 이미지의 접근 URL
     */
    public String saveBase64Image(String base64Image, Long userId) throws IOException {
        // data URI 형식인 경우 헤더 제거 (예: "data:image/png;base64,...")
        String imageData = base64Image;
        String extension = "png"; // 기본 확장자
        
        if (base64Image.contains(",")) {
            String[] parts = base64Image.split(",");
            imageData = parts[1];
            
            // MIME 타입에서 확장자 추출
            if (parts[0].contains("image/")) {
                String mimeType = parts[0].split(";")[0].split("/")[1];
                extension = mimeType.equals("jpeg") ? "jpg" : mimeType;
            }
        }
        
        // Base64 디코딩
        byte[] imageBytes = Base64.getDecoder().decode(imageData);
        
        // 파일명 생성: timestamp_uuid.extension
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String uniqueId = UUID.randomUUID().toString().substring(0, 8);
        String filename = String.format("%s_%s.%s", timestamp, uniqueId, extension);
        
        // 사용자별 폴더 경로: uploads/images/dream/{userId}/
        Path userDir = Paths.get(uploadDir, "dream", String.valueOf(userId));
        
        // 폴더가 없으면 생성
        if (!Files.exists(userDir)) {
            Files.createDirectories(userDir);
            log.info("디렉토리 생성: {}", userDir);
        }
        
        // 파일 저장
        Path filePath = userDir.resolve(filename);
        Files.write(filePath, imageBytes);
        log.info("이미지 저장 완료: {}", filePath);
        
        // 접근 URL 반환
        String imageUrl = String.format("%s/dream/%d/%s", baseUrl, userId, filename);
        return imageUrl;
    }

    /**
     * 이미지 파일 삭제
     * @param imageUrl 삭제할 이미지 URL
     */
    public void deleteImage(String imageUrl) throws IOException {
        if (imageUrl == null || imageUrl.isEmpty()) {
            return;
        }

        // URL에서 상대 경로 추출 (절대/상대 URL 모두 처리)
        String pathPart;
        try {
            pathPart = java.net.URI.create(imageUrl).getPath();
        } catch (IllegalArgumentException e) {
            pathPart = imageUrl; // URI 파싱 실패 시 원문 사용
        }

        // baseUrl 또는 기본 경로(prefix)를 제거하여 순수 상대 경로만 남김
        if (pathPart.startsWith(baseUrl)) {
            pathPart = pathPart.substring(baseUrl.length());
        } else if (pathPart.startsWith("/uploads/images")) {
            pathPart = pathPart.substring("/uploads/images".length());
        }

        // 선행 슬래시 제거
        if (pathPart.startsWith("/")) {
            pathPart = pathPart.substring(1);
        }

        Path filePath = Paths.get(uploadDir, pathPart);

        if (Files.exists(filePath)) {
            Files.delete(filePath);
            log.info("이미지 삭제 완료: {}", filePath);
        } else {
            log.warn("삭제 대상 이미지가 존재하지 않습니다: {}", filePath);
        }
    }
}
