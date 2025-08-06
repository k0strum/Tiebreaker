package com.Tiebreaker.controller.auth;

import com.Tiebreaker.dto.auth.MemberCreateRequest;
import com.Tiebreaker.dto.auth.LoginRequest;
import com.Tiebreaker.dto.auth.LoginResponse;
import com.Tiebreaker.dto.auth.MemberResponse;
import com.Tiebreaker.dto.auth.ErrorResponse;
import com.Tiebreaker.service.auth.MemberService;
import com.Tiebreaker.exception.auth.LoginException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
@Validated
public class MemberController {

  private final MemberService memberService;

  // 회원가입
  @PostMapping("/join")
  public ResponseEntity<?> join(@Valid @RequestBody MemberCreateRequest request) {
    log.info("회원가입 요청: {}", request.getEmail());
    
    try {
      MemberResponse response = memberService.join(request);
      log.info("회원가입 성공: {}", response.getEmail());
      
      return ResponseEntity.status(HttpStatus.CREATED)
        .body("회원가입이 완료되었습니다. 이메일을 확인하여 인증을 완료해주세요.");
      
    } catch (IllegalArgumentException e) {
      log.warn("회원가입 실패 - 입력값 오류: {}", e.getMessage());
      throw e;
      
    } catch (IllegalStateException e) {
      log.warn("회원가입 실패 - 중복 데이터: {}", e.getMessage());
      throw e;
      
    } catch (Exception e) {
      log.error("회원가입 실패 - 서버 오류: {}", e.getMessage(), e);
      throw new RuntimeException("회원가입 중 오류가 발생했습니다.", e);
    }
  }

  // 로그인
  @PostMapping("/login")
  public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
    log.info("로그인 요청: {}", request.getEmail());
    
    try {
      LoginResponse response = memberService.login(request);
      log.info("로그인 성공: {}", request.getEmail());
      
      return ResponseEntity.ok(response);

    } catch (LoginException e) {
      log.warn("로그인 실패 - 인증 오류: {} (타입: {})", e.getMessage(), e.getErrorType());
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(ErrorResponse.builder()
          .message(e.getMessage())
          .error(e.getErrorType().name())
          .status(400)
          .build());

    } catch (Exception e) {
      log.error("로그인 실패 - 서버 오류: {}", e.getMessage(), e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(ErrorResponse.builder()
          .message("로그인 중 오류가 발생했습니다.")
          .error("INTERNAL_SERVER_ERROR")
          .status(500)
          .build());
    }
  }

  // 현재 로그인한 사용자 정보 조회
  @GetMapping("/me")
  public ResponseEntity<?> getCurrentMember() {
    try {
      MemberResponse response = memberService.getCurrentMember();
      return ResponseEntity.ok(response);
    } catch (Exception e) {
      log.error("사용자 정보 조회 실패: {}", e.getMessage(), e);
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
        .body(ErrorResponse.builder()
          .message("인증이 필요합니다.")
          .error("UNAUTHORIZED")
          .status(401)
          .build());
    }
  }

}
