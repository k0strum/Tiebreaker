package com.Tiebreaker.entity.auth;

import com.Tiebreaker.constant.VerificationType;
import com.Tiebreaker.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "email_verification")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class EmailVerification extends BaseTimeEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;
    
    @Column(name = "email", nullable = false)
    private String email;
    
    @Column(name = "token", nullable = false, unique = true)
    private String token;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "verification_type", nullable = false)
    private VerificationType type;
    
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;
    
    @Column(name = "is_verified", nullable = false)
    private boolean verified = false;
    
    @Column(name = "verified_at")
    private LocalDateTime verifiedAt;
    
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(this.expiresAt);
    }
    
    public boolean isValid() {
        return !isExpired() && !this.verified;
    }
    
    public void markAsVerified() {
        this.verified = true;
        this.verifiedAt = LocalDateTime.now();
    }
} 