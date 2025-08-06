package com.Tiebreaker.exception.auth;

public class LoginException extends RuntimeException {
    
    private final LoginErrorType errorType;
    
    public LoginException(LoginErrorType errorType, String message) {
        super(message);
        this.errorType = errorType;
    }
    
    public LoginErrorType getErrorType() {
        return errorType;
    }
    
    public enum LoginErrorType {
        MEMBER_NOT_FOUND,           // 회원이 존재하지 않음
        SOCIAL_LOGIN_REQUIRED,      // 소셜 로그인 필요
        EMAIL_NOT_VERIFIED,         // 이메일 미인증
        INVALID_PASSWORD,           // 비밀번호 불일치
        INVALID_CREDENTIALS         // 일반적인 인증 실패 (보안상 구체적 정보 제공 안함)
    }
}
