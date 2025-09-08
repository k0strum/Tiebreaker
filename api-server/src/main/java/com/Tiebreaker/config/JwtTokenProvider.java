package com.Tiebreaker.config;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.SignatureException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.util.*;
import javax.crypto.SecretKey;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    // 토큰 생성 시 사용되는 시크릿 키
    @Value("${jwt.secret}")
    private String secretKey;

    // 토큰 유효 기간
    @Value("${jwt.validity}")
    private long validityInMilliseconds;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretKey));
    }

    public String createToken(String email, String role) {
        Date now = new Date();
        Date validity = new Date(now.getTime() + validityInMilliseconds);
        SecretKey key = getSigningKey();
        return Jwts.builder()
                .subject(email)
                .claim("role", role)
                .issuedAt(now)
                .expiration(validity)
                .signWith(key, Jwts.SIG.HS256)
                .compact();
    }

    public String getEmail(String token) {
        SecretKey key = getSigningKey();
        return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload().getSubject();
    }

    public boolean validateToken(String token) {
        try {
            SecretKey key = getSigningKey();
            Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            System.out.println("[JWT] 만료된 토큰입니다.");
        } catch (UnsupportedJwtException e) {
            System.out.println("[JWT] 지원하지 않는 토큰입니다.");
        } catch (MalformedJwtException e) {
            System.out.println("[JWT] 잘못된 형식의 토큰입니다.");
        } catch (SignatureException e) {
            System.out.println("[JWT] 서명이 올바르지 않습니다.");
        } catch (IllegalArgumentException e) {
            System.out.println("[JWT] 잘못된 토큰입니다.");
        }
        return false;
    }
}