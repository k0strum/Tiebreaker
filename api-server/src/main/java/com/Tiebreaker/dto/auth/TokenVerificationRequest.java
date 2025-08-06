package com.Tiebreaker.dto.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TokenVerificationRequest {
    
    @NotBlank(message = "인증 토큰은 필수입니다.")
    private String token;
    
    private String verificationType;
} 