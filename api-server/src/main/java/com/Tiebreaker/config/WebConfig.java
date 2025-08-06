package com.Tiebreaker.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 업로드된 이미지 파일들을 서빙하기 위한 설정
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:uploads/");
        
        // 프로필 이미지 접근을 위한 별도 경로
        registry.addResourceHandler("/profile/**")
                .addResourceLocations("file:uploads/");
    }
}
