package com.Tiebreaker.service.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class CustomAuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    private final ObjectMapper objectMapper;

        @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                       AuthenticationException exception) throws IOException, ServletException {
        
        String errorMessage = "인증에 실패했습니다.";
        String errorCode = "AUTHENTICATION_FAILED";
        
        if (exception instanceof OAuth2AuthenticationException) {
            OAuth2AuthenticationException oauthException = (OAuth2AuthenticationException) exception;
            String oauthErrorCode = oauthException.getError().getErrorCode();
            
            if ("email_exists".equals(oauthErrorCode)) {
                errorMessage = "이미 해당 이메일로 가입된 계정이 있습니다. 일반 로그인을 이용해 주세요.";
                errorCode = "EMAIL_EXISTS";
            } else {
                errorMessage = "소셜 로그인에 실패했습니다: " + oauthException.getError().getDescription();
                errorCode = "OAUTH_ERROR";
            }
        } else {
            errorMessage = "인증에 실패했습니다: " + exception.getMessage();
        }
        
        // 프론트엔드로 리다이렉트 (에러 정보를 URL 파라미터로 전달)
        String frontendUrl = "http://localhost:5173";
        String redirectUrl = String.format("%s/oauth-callback?success=false&errorCode=%s&message=%s", 
            frontendUrl, errorCode, java.net.URLEncoder.encode(errorMessage, "UTF-8"));
        
        response.sendRedirect(redirectUrl);
    }
}
