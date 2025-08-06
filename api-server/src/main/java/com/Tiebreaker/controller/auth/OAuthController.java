package com.Tiebreaker.controller.auth;

import com.Tiebreaker.entity.auth.Member;
import com.Tiebreaker.repository.MemberRepository;
import com.Tiebreaker.service.auth.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/oauth")
@RequiredArgsConstructor
public class OAuthController {

    private final MemberRepository memberRepository;
    private final MemberService memberService;

    @GetMapping("/login-urls")
    public ResponseEntity<Map<String, String>> getLoginUrls() {
        Map<String, String> loginUrls = new HashMap<>();
        
        // 각 소셜 로그인 URL 설정
        loginUrls.put("google", "/oauth2/authorization/google");
        loginUrls.put("kakao", "/oauth2/authorization/kakao");
        loginUrls.put("naver", "/oauth2/authorization/naver");
        
        return ResponseEntity.ok(loginUrls);
    }

    @PostMapping("/update-phone")
    public ResponseEntity<Map<String, Object>> updatePhone(
            @RequestParam String email,
            @RequestParam String phone) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            Optional<Member> memberOpt = memberRepository.findByEmail(email);
            if (memberOpt.isPresent()) {
                Member member = memberOpt.get();
                member.setPhone(phone);
                memberRepository.save(member);
                
                response.put("success", true);
                response.put("message", "전화번호가 성공적으로 업데이트되었습니다.");
                response.put("member", member.toResponse());
                
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "회원을 찾을 수 없습니다.");
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "전화번호 업데이트 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @GetMapping("/check-phone")
    public ResponseEntity<Map<String, Object>> checkPhone(@RequestParam String email) {
        Map<String, Object> response = new HashMap<>();
        
        Optional<Member> memberOpt = memberRepository.findByEmail(email);
        if (memberOpt.isPresent()) {
            Member member = memberOpt.get();
            boolean needsPhoneInput = "000-0000-0000".equals(member.getPhone());
            
            response.put("success", true);
            response.put("needsPhoneInput", needsPhoneInput);
            response.put("phone", member.getPhone());
            
            return ResponseEntity.ok(response);
        } else {
            response.put("success", false);
            response.put("message", "회원을 찾을 수 없습니다.");
            return ResponseEntity.badRequest().body(response);
        }
    }
}
