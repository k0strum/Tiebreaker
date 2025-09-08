package com.Tiebreaker.service.auth;

import com.Tiebreaker.constant.VerificationType;
import com.Tiebreaker.entity.auth.EmailVerification;
import com.Tiebreaker.entity.auth.Member;
import com.Tiebreaker.repository.auth.EmailVerificationRepository;
import com.Tiebreaker.repository.auth.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailVerificationService {

    private final EmailVerificationRepository emailVerificationRepository;
    private final MemberRepository memberRepository;
    private final EmailService emailService;

    @Transactional
    public String createVerificationToken(String email, Long memberId, VerificationType type, String nickname) {
        // 기존 미인증 토큰 삭제
        emailVerificationRepository.deleteUnverifiedTokensByEmail(email);

        // 새 토큰 생성
        String token = generateToken();
        LocalDateTime expiresAt = LocalDateTime.now().plusHours(24); // 24시간 유효

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

        EmailVerification verification = EmailVerification.builder()
                .member(member)
                .email(email)
                .token(token)
                .type(type)
                .expiresAt(expiresAt)
                .verified(false)
                .build();

        emailVerificationRepository.save(verification);

        // 이메일 발송
        emailService.sendVerificationEmail(email, token, type, nickname);

        log.info("인증 토큰 생성 완료: email={}, type={}", email, type);
        return token;
    }

    @Transactional
    public boolean verifyToken(String token) {
        Optional<EmailVerification> verificationOpt = emailVerificationRepository.findByToken(token);

        if (verificationOpt.isEmpty()) {
            log.warn("존재하지 않는 토큰: {}", token);
            return false;
        }

        EmailVerification verification = verificationOpt.get();

        if (!verification.isValid()) {
            log.warn("유효하지 않은 토큰: token={}, expired={}, verified={}",
                    token, verification.isExpired(), verification.isVerified());
            return false;
        }

        // 토큰 인증 완료 처리
        verification.markAsVerified();
        emailVerificationRepository.save(verification);

        // 회원 이메일 인증 상태 업데이트 (회원가입인 경우)
        if (verification.getType() == VerificationType.SIGNUP) {
            Member member = verification.getMember();
            member.setEmailVerified(true);
            memberRepository.save(member);
            log.info("회원 이메일 인증 완료: memberId={}, email={}", member.getId(), member.getEmail());
        }

        log.info("토큰 인증 완료: token={}, type={}", token, verification.getType());
        return true;
    }

    public Optional<EmailVerification> findValidToken(String email, VerificationType type) {
        return emailVerificationRepository.findByEmailAndTypeAndVerifiedFalseAndExpiresAtAfter(
                email, type, LocalDateTime.now());
    }

    @Transactional
    public String resendVerificationToken(String email, VerificationType type, String nickname) {
        // 회원 정보 조회
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

        // 이미 이메일 인증이 완료된 사용자라면 재전송 차단 (회원가입 인증의 경우)
        if (type == VerificationType.SIGNUP && member.isEmailVerified()) {
            throw new IllegalStateException("이미 이메일 인증이 완료된 계정입니다.");
        }

        // 기존 미인증 토큰 삭제 후 새 토큰 생성
        emailVerificationRepository.deleteUnverifiedTokensByEmail(email);
        return createVerificationToken(email, member.getId(), type, nickname);
    }

    private String generateToken() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    // 매일 새벽 2시에 만료된 토큰 정리
    @Scheduled(cron = "0 0 2 * * ?")
    @Transactional
    public void cleanupExpiredTokens() {
        LocalDateTime now = LocalDateTime.now();
        List<EmailVerification> expiredTokens = emailVerificationRepository.findExpiredTokens(now);

        if (!expiredTokens.isEmpty()) {
            emailVerificationRepository.deleteExpiredTokens(now);
            log.info("만료된 토큰 {}개 정리 완료", expiredTokens.size());
        }
    }

    public Optional<EmailVerification> findByToken(String token) {
        return emailVerificationRepository.findByToken(token);
    }
}