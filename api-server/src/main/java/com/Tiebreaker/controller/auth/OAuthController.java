package com.Tiebreaker.controller.auth;

import com.Tiebreaker.entity.auth.Member;
import com.Tiebreaker.repository.auth.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * OAuth 소셜 로그인 관련 API 컨트롤러
 * - 소셜 로그인 URL 제공
 * - 소셜 로그인 후 추가 정보 입력 처리
 * - 소셜 로그인 상태 확인
 */
@Slf4j
@RestController
@RequestMapping("/api/oauth")
@RequiredArgsConstructor
public class OAuthController {

    private final MemberRepository memberRepository;

    /**
     * 소셜 로그인 URL 목록 조회
     * 
     * @return 각 소셜 로그인 제공자의 인증 URL
     */
    @GetMapping("/login-urls")
    public ResponseEntity<Map<String, Object>> getLoginUrls() {
        Map<String, Object> response = new HashMap<>();
        Map<String, String> loginUrls = new HashMap<>();

        // 각 소셜 로그인 URL 설정
        loginUrls.put("google", "/oauth2/authorization/google");
        loginUrls.put("kakao", "/oauth2/authorization/kakao");
        loginUrls.put("naver", "/oauth2/authorization/naver");

        response.put("success", true);
        response.put("loginUrls", loginUrls);
        response.put("message", "소셜 로그인 URL을 성공적으로 조회했습니다.");

        log.info("소셜 로그인 URL 조회 요청");
        return ResponseEntity.ok(response);
    }

    /**
     * 소셜 로그인 사용자의 전화번호 업데이트
     * 
     * @param email 사용자 이메일
     * @param phone 업데이트할 전화번호
     * @return 업데이트 결과
     */
    @PostMapping("/update-phone")
    public ResponseEntity<Map<String, Object>> updatePhone(
            @RequestParam String email,
            @RequestParam String phone) {

        Map<String, Object> response = new HashMap<>();

        try {
            // 입력값 검증
            if (email == null || email.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "이메일이 필요합니다.");
                return ResponseEntity.badRequest().body(response);
            }

            if (phone == null || phone.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "전화번호가 필요합니다.");
                return ResponseEntity.badRequest().body(response);
            }

            // 전화번호 형식 검증 (간단한 검증)
            if (!phone.matches("^\\d{3}-\\d{4}-\\d{4}$")) {
                response.put("success", false);
                response.put("message", "전화번호 형식이 올바르지 않습니다. (예: 010-1234-5678)");
                return ResponseEntity.badRequest().body(response);
            }

            Optional<Member> memberOpt = memberRepository.findByEmail(email);
            if (memberOpt.isPresent()) {
                Member member = memberOpt.get();

                // 소셜 로그인 사용자인지 확인
                if (member.getLoginType() == null || "LOCAL".equals(member.getLoginType())) {
                    response.put("success", false);
                    response.put("message", "소셜 로그인 사용자만 전화번호를 업데이트할 수 있습니다.");
                    return ResponseEntity.badRequest().body(response);
                }

                // 전화번호 중복 확인
                Optional<Member> existingPhoneMember = memberRepository.findByPhone(phone);
                if (existingPhoneMember.isPresent() && !existingPhoneMember.get().getId().equals(member.getId())) {
                    response.put("success", false);
                    response.put("message", "이미 사용 중인 전화번호입니다.");
                    return ResponseEntity.badRequest().body(response);
                }

                member.setPhone(phone);
                memberRepository.save(member);

                response.put("success", true);
                response.put("message", "전화번호가 성공적으로 업데이트되었습니다.");
                response.put("member", member.toResponse());

                log.info("소셜 로그인 사용자 전화번호 업데이트 완료: {}", email);
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "회원을 찾을 수 없습니다.");
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            log.error("전화번호 업데이트 중 오류 발생: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("message", "전화번호 업데이트 중 오류가 발생했습니다.");
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 소셜 로그인 사용자의 전화번호 입력 필요 여부 확인
     * 
     * @param email 사용자 이메일
     * @return 전화번호 입력 필요 여부 및 현재 전화번호
     */
    @GetMapping("/check-phone")
    public ResponseEntity<Map<String, Object>> checkPhone(@RequestParam String email) {
        Map<String, Object> response = new HashMap<>();

        try {
            // 입력값 검증
            if (email == null || email.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "이메일이 필요합니다.");
                return ResponseEntity.badRequest().body(response);
            }

            Optional<Member> memberOpt = memberRepository.findByEmail(email);
            if (memberOpt.isPresent()) {
                Member member = memberOpt.get();

                // 소셜 로그인 사용자인지 확인
                if (member.getLoginType() == null || "LOCAL".equals(member.getLoginType())) {
                    response.put("success", false);
                    response.put("message", "소셜 로그인 사용자가 아닙니다.");
                    return ResponseEntity.badRequest().body(response);
                }

                // 전화번호 입력 필요 여부 확인 (null이거나 빈 문자열인 경우)
                boolean needsPhoneInput = member.getPhone() == null ||
                        member.getPhone().trim().isEmpty() ||
                        "000-0000-0000".equals(member.getPhone());

                response.put("success", true);
                response.put("needsPhoneInput", needsPhoneInput);
                response.put("phone", member.getPhone());
                response.put("loginType", member.getLoginType());
                response.put("message", needsPhoneInput ? "전화번호 입력이 필요합니다." : "전화번호가 등록되어 있습니다.");

                log.info("소셜 로그인 사용자 전화번호 확인: {} - 필요여부: {}", email, needsPhoneInput);
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "회원을 찾을 수 없습니다.");
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            log.error("전화번호 확인 중 오류 발생: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("message", "전화번호 확인 중 오류가 발생했습니다.");
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 소셜 로그인 사용자의 추가 정보 업데이트 (닉네임, 전화번호, 주소)
     * 
     * @param email    사용자 이메일
     * @param nickname 닉네임
     * @param phone    전화번호
     * @param address  주소
     * @return 업데이트 결과
     */
    @PostMapping("/update-additional-info")
    public ResponseEntity<Map<String, Object>> updateAdditionalInfo(
            @RequestParam String email,
            @RequestParam(required = false) String nickname,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) String address) {

        Map<String, Object> response = new HashMap<>();

        try {
            // 입력값 검증
            if (email == null || email.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "이메일이 필요합니다.");
                return ResponseEntity.badRequest().body(response);
            }

            Optional<Member> memberOpt = memberRepository.findByEmail(email);
            if (memberOpt.isPresent()) {
                Member member = memberOpt.get();

                // 소셜 로그인 사용자인지 확인
                if (member.getLoginType() == null || "LOCAL".equals(member.getLoginType())) {
                    response.put("success", false);
                    response.put("message", "소셜 로그인 사용자만 추가 정보를 업데이트할 수 있습니다.");
                    return ResponseEntity.badRequest().body(response);
                }

                // 닉네임 업데이트
                if (nickname != null && !nickname.trim().isEmpty()) {
                    // 닉네임 중복 확인
                    Optional<Member> existingNicknameMember = memberRepository.findByNickname(nickname);
                    if (existingNicknameMember.isPresent()
                            && !existingNicknameMember.get().getId().equals(member.getId())) {
                        response.put("success", false);
                        response.put("message", "이미 사용 중인 닉네임입니다.");
                        return ResponseEntity.badRequest().body(response);
                    }
                    member.setNickname(nickname.trim());
                }

                // 전화번호 업데이트
                if (phone != null && !phone.trim().isEmpty()) {
                    // 전화번호 형식 검증
                    if (!phone.matches("^\\d{3}-\\d{4}-\\d{4}$")) {
                        response.put("success", false);
                        response.put("message", "전화번호 형식이 올바르지 않습니다. (예: 010-1234-5678)");
                        return ResponseEntity.badRequest().body(response);
                    }

                    // 전화번호 중복 확인
                    Optional<Member> existingPhoneMember = memberRepository.findByPhone(phone);
                    if (existingPhoneMember.isPresent() && !existingPhoneMember.get().getId().equals(member.getId())) {
                        response.put("success", false);
                        response.put("message", "이미 사용 중인 전화번호입니다.");
                        return ResponseEntity.badRequest().body(response);
                    }
                    member.setPhone(phone);
                }

                // 주소 업데이트
                if (address != null) {
                    member.setAddress(address.trim());
                }

                memberRepository.save(member);

                response.put("success", true);
                response.put("message", "추가 정보가 성공적으로 업데이트되었습니다.");
                response.put("member", member.toResponse());

                log.info("소셜 로그인 사용자 추가 정보 업데이트 완료: {}", email);
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "회원을 찾을 수 없습니다.");
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            log.error("추가 정보 업데이트 중 오류 발생: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("message", "추가 정보 업데이트 중 오류가 발생했습니다.");
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 소셜 로그인 사용자 정보 조회
     * 
     * @param email 사용자 이메일
     * @return 사용자 정보
     */
    @GetMapping("/user-info")
    public ResponseEntity<Map<String, Object>> getUserInfo(@RequestParam String email) {
        Map<String, Object> response = new HashMap<>();

        try {
            if (email == null || email.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "이메일이 필요합니다.");
                return ResponseEntity.badRequest().body(response);
            }

            Optional<Member> memberOpt = memberRepository.findByEmail(email);
            if (memberOpt.isPresent()) {
                Member member = memberOpt.get();

                // 소셜 로그인 사용자인지 확인
                if (member.getLoginType() == null || "LOCAL".equals(member.getLoginType())) {
                    response.put("success", false);
                    response.put("message", "소셜 로그인 사용자가 아닙니다.");
                    return ResponseEntity.badRequest().body(response);
                }

                response.put("success", true);
                response.put("member", member.toResponse());
                response.put("message", "사용자 정보를 성공적으로 조회했습니다.");

                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "회원을 찾을 수 없습니다.");
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            log.error("사용자 정보 조회 중 오류 발생: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("message", "사용자 정보 조회 중 오류가 발생했습니다.");
            return ResponseEntity.internalServerError().body(response);
        }
    }
}
