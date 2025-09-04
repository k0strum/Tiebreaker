package com.Tiebreaker.service.auth;

import com.Tiebreaker.config.JwtTokenProvider;
import com.Tiebreaker.entity.auth.Member;
import com.Tiebreaker.repository.MemberRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class CustomAuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtTokenProvider jwtTokenProvider;
    private final MemberRepository memberRepository;
    private final ObjectMapper objectMapper;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String email = extractEmail(oAuth2User);

        Optional<Member> memberOpt = memberRepository.findByEmail(email);
        if (memberOpt.isPresent()) {
            Member member = memberOpt.get();

            // JWT 토큰 생성
            String token = jwtTokenProvider.createToken(member.getEmail(), member.getRole().toString(),
                    member.getId().toString());

            // 프론트엔드로 리다이렉트 (토큰을 URL 파라미터로 전달)
            String frontendUrl = "http://localhost:5173";
            String redirectUrl = String.format("%s/oauth-callback?token=%s&loginType=%s&success=true",
                    frontendUrl,
                    java.net.URLEncoder.encode(token, "UTF-8"),
                    java.net.URLEncoder.encode(member.getLoginType(), "UTF-8"));

            response.sendRedirect(redirectUrl);
        } else {
            // 회원 정보가 없는 경우 (이론적으로는 발생하지 않아야 함)
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "회원 정보를 찾을 수 없습니다.");
            objectMapper.writeValue(response.getWriter(), errorResponse);
        }
    }

    private String extractEmail(OAuth2User oAuth2User) {
        Map<String, Object> attributes = oAuth2User.getAttributes();

        // 카카오
        if (attributes.containsKey("kakao_account")) {
            Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
            return (String) kakaoAccount.get("email");
        }

        // 네이버
        if (attributes.containsKey("response")) {
            Map<String, Object> response = (Map<String, Object>) attributes.get("response");
            return (String) response.get("email");
        }

        // 구글 등 기타
        return (String) attributes.get("email");
    }
}
