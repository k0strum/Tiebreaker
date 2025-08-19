package com.Tiebreaker.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final org.springframework.security.core.userdetails.UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // 인증이 필요하지 않은 경로는 필터를 건너뛰기
        String requestURI = request.getRequestURI();
        if (shouldNotFilter(requestURI)) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            // 1. 요청에서 JWT 토큰 추출
            String token = getTokenFromRequest(request);

            // 2. 토큰이 존재하고 유효한 경우
            if (StringUtils.hasText(token) && jwtTokenProvider.validateToken(token)) {
                // 3. 토큰에서 사용자 이메일 추출
                String email = jwtTokenProvider.getEmail(token);

                // 4. UserDetails 로드
                UserDetails userDetails = userDetailsService.loadUserByUsername(email);

                // 5. 인증 객체 생성
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // 6. SecurityContext에 인증 정보 설정
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (Exception e) {
            logger.error("JWT 토큰 처리 중 오류 발생: " + e.getMessage());
        }

        filterChain.doFilter(request, response);
    }

    // 인증이 필요하지 않은 경로인지 확인
    private boolean shouldNotFilter(String requestURI) {
        return requestURI.equals("/api/members/join") ||
                requestURI.equals("/api/members/login") ||
                requestURI.equals("/api/info/current/teamRank") ||
                requestURI.startsWith("/api/rankings/") ||
                requestURI.startsWith("/api/player/") ||
                requestURI.startsWith("/actuator/") ||
                requestURI.equals("/") ||
                requestURI.startsWith("/css/") ||
                requestURI.startsWith("/js/") ||
                requestURI.startsWith("/img/") ||
                requestURI.startsWith("/images/") ||
                requestURI.startsWith("/profile/") ||
                requestURI.startsWith("/static/") ||
                requestURI.endsWith(".html") ||
                requestURI.endsWith(".ico");
    }

    private String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}