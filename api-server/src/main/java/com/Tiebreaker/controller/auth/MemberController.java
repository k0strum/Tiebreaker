package com.Tiebreaker.controller.auth;

import com.Tiebreaker.dto.auth.MemberCreateRequest;
import com.Tiebreaker.dto.auth.MemberResponse;
import com.Tiebreaker.service.auth.MemberService;
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
  public ResponseEntity<MemberResponse> join(@Valid @RequestBody MemberCreateRequest request) {
    log.info("회원가입 요청: {}", request.getEmail());
    
    try {
      MemberResponse response = memberService.join(request);
      log.info("회원가입 성공: {}", response.getEmail());
      
      return ResponseEntity.status(HttpStatus.CREATED).body(response);
      
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
}
