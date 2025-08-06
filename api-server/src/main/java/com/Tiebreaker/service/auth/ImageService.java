package com.Tiebreaker.service.auth;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class ImageService {
    
    private final Path uploadPath = Paths.get("uploads");
    private final RestTemplate restTemplate = new RestTemplate();
    
    public ImageService() {
        try {
            Files.createDirectories(uploadPath);
        } catch (IOException e) {
            throw new RuntimeException("Could not create upload directory", e);
        }
    }
    
    public String downloadAndSaveImage(String imageUrl, String type) {
        if (imageUrl == null || imageUrl.trim().isEmpty()) {
            return null;
        }
        
        try {
            // 파일 확장자 추출
            String extension = getFileExtension(imageUrl);
            if (extension == null) {
                extension = "jpg"; // 기본값
            }
            
            // 고유한 파일명 생성
            String fileName = type + "_" + UUID.randomUUID().toString() + "." + extension;
            Path targetPath = uploadPath.resolve(fileName);
            
            // 이미지 다운로드
            URL url = new URL(imageUrl);
            try (InputStream inputStream = url.openStream()) {
                Files.copy(inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);
            }
            
            return fileName;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    private String getFileExtension(String url) {
        if (url == null) return null;
        
        int lastDotIndex = url.lastIndexOf('.');
        if (lastDotIndex > 0) {
            String extension = url.substring(lastDotIndex + 1);
            // URL 파라미터 제거
            int paramIndex = extension.indexOf('?');
            if (paramIndex > 0) {
                extension = extension.substring(0, paramIndex);
            }
            return extension;
        }
        return null;
    }
    
    public Resource loadImageAsResource(String fileName) {
        try {
            Path filePath = uploadPath.resolve(fileName);
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists()) {
                return resource;
            } else {
                throw new RuntimeException("File not found: " + fileName);
            }
        } catch (IOException e) {
            throw new RuntimeException("File not found: " + fileName, e);
        }
    }
}
