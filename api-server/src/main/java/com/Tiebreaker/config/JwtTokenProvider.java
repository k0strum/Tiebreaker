package com.Tiebreaker.config;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.SignatureException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.*;
import javax.crypto.SecretKey;

@Component
public class JwtTokenProvider {
    
    private static final Logger logger = LoggerFactory.getLogger(JwtTokenProvider.class);
    
    @Value("${jwt.secret}")
    private String secretKey;
    
    @Value("${jwt.validity}")
    private long validityInMilliseconds;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretKey));
    }

    public String createToken(String email, List<String> roles) {
        Date now = new Date();
        Date validity = new Date(now.getTime() + validityInMilliseconds);
        SecretKey key = getSigningKey();
        return Jwts.builder()
                .subject(email)
                .claim("roles", roles)
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
            logger.warn("[JWT] 만료된 토큰입니다.");
        } catch (UnsupportedJwtException e) {
            logger.warn("[JWT] 지원하지 않는 토큰입니다.");
        } catch (MalformedJwtException e) {
            logger.warn("[JWT] 잘못된 형식의 토큰입니다.");
        } catch (SignatureException e) {
            logger.warn("[JWT] 서명이 올바르지 않습니다.");
        } catch (IllegalArgumentException e) {
            logger.warn("[JWT] 잘못된 토큰입니다.");
        }
        return false;
    }
} 