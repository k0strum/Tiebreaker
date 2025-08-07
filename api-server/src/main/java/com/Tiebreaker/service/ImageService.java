package com.Tiebreaker.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
@Slf4j
public class ImageService {

    // application.properties에서 설정한 경로를 주입받습니다.
    @Value("${app.upload.profile-image.location}")
    private String profileImgLocation;

    /**
     * MultipartFile을 서버에 업로드하고 웹 경로를 반환합니다.
     * @param multipartFile 업로드할 이미지 파일
     * @param locationType "profile" 또는 "item" 등 저장할 위치 타입
     * @return 웹에서 접근 가능한 이미지 URL 경로
     * @throws Exception
     */
    public String uploadFile(MultipartFile multipartFile, String locationType) throws Exception {
        if (multipartFile == null || multipartFile.isEmpty()) {
            return null; // 업로드된 파일이 없으면 null 반환
        }
        String originalFileName = multipartFile.getOriginalFilename();
        byte[] fileData = multipartFile.getBytes();
        String savedFileName = saveFile(originalFileName, fileData, locationType);

        // locationType에 따라 다른 웹 경로를 반환합니다.
        if ("profile".equals(locationType)) {
            return "/api/members/images/profile/" + savedFileName;
        }
        return null;
    }

    /**
     * URL로부터 이미지를 다운로드하여 서버에 저장하고 웹 경로를 반환합니다.
     * @param imageUrl 다운로드할 이미지의 전체 URL
     * @param locationType "profile" 또는 "item" 등 저장할 위치 타입
     * @return 웹에서 접근 가능한 이미지 URL 경로
     */
    public String downloadAndSaveImage(String imageUrl, String locationType) {
        if (!StringUtils.hasText(imageUrl)) {
            return null; // URL이 비어있으면 null 반환
        }
        try (InputStream in = new URL(imageUrl).openStream()) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int n;
            while ((n = in.read(buffer)) != -1) {
                out.write(buffer, 0, n);
            }
            byte[] fileData = out.toByteArray();

            // 파일 이름은 URL에서 마지막 부분을 사용하거나, 없으면 UUID로 생성
            String fileName = imageUrl.substring(imageUrl.lastIndexOf('/') + 1);
            if (!StringUtils.hasText(fileName) || fileName.length() > 100) { // 너무 길거나 이름이 없는 경우
                fileName = UUID.randomUUID().toString() + ".jpg";
            }

            String savedFileName = saveFile(fileName, fileData, locationType);

            if ("profile".equals(locationType)) {
                return "/api/members/images/profile/" + savedFileName;
            }
            return null;

        } catch (Exception e) {
            log.error("Failed to download and save image from URL: " + imageUrl, e);
            return null; // 실패 시 null 반환
        }
    }

    /**
     * 프로필 이미지 전용 다운로드 메서드
     */
    public String downloadAndSaveProfileImage(String imageUrl) {
        return downloadAndSaveImage(imageUrl, "profile");
    }

    /**
     * MultipartFile을 프로필 이미지로 업로드하는 메서드
     */
    public String uploadProfileImage(MultipartFile multipartFile) {
        try {
            return uploadFile(multipartFile, "profile");
        } catch (Exception e) {
            log.error("프로필 이미지 업로드 실패", e);
            return null;
        }
    }

    /**
     * 실제 파일을 저장하고, 고유한 파일명을 반환하는 핵심 로직
     */
    private String saveFile(String originalFileName, byte[] fileData, String locationType) throws Exception {
        String uploadPath;
        if ("profile".equals(locationType)) {
            uploadPath = profileImgLocation;
        } else {
            throw new IllegalArgumentException("Invalid location type: " + locationType);
        }

        UUID uuid = UUID.randomUUID();

        String extension;
        int dotIndex = originalFileName.lastIndexOf(".");

        // 파일 이름에 '.'이 있고, 그 위치가 파일 이름의 마지막이 아닌 경우에만 확장자로 인정합니다.
        if (dotIndex > -1 && dotIndex < originalFileName.length() - 1) {
            extension = originalFileName.substring(dotIndex);
        } else {
            // 확장자가 없는 경우, 기본적으로 .jpg를 붙여줍니다. (소셜 프로필 사진은 대부분 jpg 또는 png)
            extension = ".jpg";
        }

        String savedFileName = uuid.toString() + extension;
        String fileUploadFullUrl = uploadPath + savedFileName;

        // 폴더가 없으면 생성
        File uploadDir = new File(uploadPath);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }

        // 파일 저장
        try (FileOutputStream fos = new FileOutputStream(fileUploadFullUrl)) {
            fos.write(fileData);
        }

        return savedFileName; // 저장된 파일의 이름(UUID.확장자) 반환
    }

    /**
     * 파일을 Resource로 로드하는 메서드
     */
    public Resource loadImageAsResource(String fileName, String locationType) {
        try {
            String uploadPath;
            if ("profile".equals(locationType)) {
                uploadPath = profileImgLocation;
            } else {
                throw new IllegalArgumentException("Invalid location type: " + locationType);
            }

            Path filePath = Paths.get(uploadPath).resolve(fileName);
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

    /**
     * 프로필 이미지 전용 리소스 로드 메서드
     */
    public Resource loadProfileImageAsResource(String fileName) {
        return loadImageAsResource(fileName, "profile");
    }

    /**
     * 파일 삭제 로직
     */
    public void deleteFile(String filePath, String locationType) throws Exception {
        if (!StringUtils.hasText(filePath)) return;

        String fileName;
        String uploadPath;

        if ("profile".equals(locationType)) {
            if (filePath.startsWith("/api/members/images/profile/")) {
                fileName = filePath.replace("/api/members/images/profile/", "");
                uploadPath = profileImgLocation;
            } else {
                return;
            }
        } else {
            return;
        }

        String absolutePath = uploadPath + fileName;
        File file = new File(absolutePath);
        if (file.exists()) {
            boolean deleted = file.delete();
            if (!deleted) {
                throw new Exception("이미지 파일 삭제 실패: " + absolutePath);
            }
        }
    }

    /**
     * 프로필 이미지 URL을 생성합니다.
     * @param fileName 파일명
     * @return 완전한 이미지 URL
     */
    public String getProfileImageUrl(String fileName) {
        if (fileName == null || fileName.trim().isEmpty()) {
            return "/api/members/images/profile-default.svg";
        }
        
        // 기본 이미지인 경우
        if ("profile-default.svg".equals(fileName)) {
            return "/api/members/images/profile-default.svg";
        }
        
        return "/api/members/images/profile/" + fileName;
    }
}
