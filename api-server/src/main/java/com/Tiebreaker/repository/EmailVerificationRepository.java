package com.Tiebreaker.repository;

import com.Tiebreaker.constant.VerificationType;
import com.Tiebreaker.entity.auth.EmailVerification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface EmailVerificationRepository extends JpaRepository<EmailVerification, Long> {
    
    Optional<EmailVerification> findByToken(String token);
    
    Optional<EmailVerification> findByEmailAndTypeAndVerifiedFalseAndExpiresAtAfter(
        String email, VerificationType type, LocalDateTime now);
    
    List<EmailVerification> findByEmailOrderByRegDateDesc(String email);
    
    @Query("SELECT ev FROM EmailVerification ev WHERE ev.expiresAt < :now")
    List<EmailVerification> findExpiredTokens(@Param("now") LocalDateTime now);
    
    @Modifying
    @Query("DELETE FROM EmailVerification ev WHERE ev.expiresAt < :now")
    void deleteExpiredTokens(@Param("now") LocalDateTime now);
    
    @Modifying
    @Query("DELETE FROM EmailVerification ev WHERE ev.email = :email AND ev.verified = false")
    void deleteUnverifiedTokensByEmail(@Param("email") String email);
} 