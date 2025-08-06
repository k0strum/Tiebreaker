package com.Tiebreaker.controller.auth;

import com.Tiebreaker.constant.VerificationType;
import com.Tiebreaker.dto.auth.EmailVerificationRequest;
import com.Tiebreaker.service.auth.EmailVerificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/email")
@RequiredArgsConstructor
@Validated
public class EmailController {
    
    private final EmailVerificationService emailVerificationService;
    
    // 이메일 인증 요청 (회원가입 시)
    @PostMapping("/verify")
    public ResponseEntity<?> sendVerificationEmail(@Valid @RequestBody EmailVerificationRequest request) {
        try {
            VerificationType type = VerificationType.valueOf(request.getVerificationType().toUpperCase());
            
            // 이미 유효한 토큰이 있는지 확인
            if (emailVerificationService.findValidToken(request.getEmail(), type).isPresent()) {
                return ResponseEntity.badRequest().body("이미 인증 이메일이 발송되었습니다. 이메일을 확인해주세요.");
            }
            
            // 토큰 생성 및 이메일 발송
            emailVerificationService.createVerificationToken(
                request.getEmail(), 
                null, // 회원가입 시에는 memberId가 없음
                type, 
                request.getNickname()
            );
            
            return ResponseEntity.ok("인증 이메일이 발송되었습니다.");
            
        } catch (IllegalArgumentException e) {
            log.error("잘못된 인증 타입: {}", request.getVerificationType());
            return ResponseEntity.badRequest().body("잘못된 인증 타입입니다.");
        } catch (Exception e) {
            log.error("이메일 발송 실패: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("이메일 발송에 실패했습니다. 다시 시도해주세요.");
        }
    }
    
    // 이메일 인증 확인 (링크 클릭 시)
    @GetMapping("/verify")
    public ResponseEntity<?> verifyEmail(@RequestParam String token) {
        try {
            boolean isValid = emailVerificationService.verifyToken(token);
            
            if (isValid) {
                return ResponseEntity.ok("이메일 인증이 완료되었습니다.");
            } else {
                return ResponseEntity.badRequest().body("유효하지 않은 인증 링크입니다.");
            }
            
        } catch (Exception e) {
            log.error("이메일 인증 실패: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("인증 처리 중 오류가 발생했습니다.");
        }
    }
    
    // 인증 이메일 재발송
    @PostMapping("/resend")
    public ResponseEntity<?> resendVerificationEmail(@Valid @RequestBody EmailVerificationRequest request) {
        try {
            VerificationType type = VerificationType.valueOf(request.getVerificationType().toUpperCase());
            
            emailVerificationService.resendVerificationToken(
                request.getEmail(), 
                type, 
                request.getNickname()
            );
            
            return ResponseEntity.ok("인증 이메일이 재발송되었습니다.");
            
        } catch (IllegalArgumentException e) {
            log.error("존재하지 않는 회원: {}", request.getEmail());
            return ResponseEntity.badRequest().body("존재하지 않는 회원입니다.");
        } catch (Exception e) {
            log.error("이메일 재발송 실패: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("이메일 재발송에 실패했습니다. 다시 시도해주세요.");
        }
    }
    
    // 토큰 상태 확인
    @GetMapping("/token-status")
    public ResponseEntity<?> checkTokenStatus(@RequestParam String token) {
        try {
            var verificationOpt = emailVerificationService.findByToken(token);
            
            if (verificationOpt.isEmpty()) {
                return ResponseEntity.ok("존재하지 않는 토큰입니다.");
            }
            
            var verification = verificationOpt.get();
            
            if (verification.isExpired()) {
                return ResponseEntity.ok("만료된 토큰입니다.");
            }
            
            if (verification.isVerified()) {
                return ResponseEntity.ok("이미 인증된 토큰입니다.");
            }
            
            return ResponseEntity.ok("유효한 토큰입니다.");
            
        } catch (Exception e) {
            log.error("토큰 상태 확인 실패: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("토큰 상태 확인 중 오류가 발생했습니다.");
        }
    }
} 