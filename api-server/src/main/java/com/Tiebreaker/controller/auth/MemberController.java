package com.Tiebreaker.controller.auth;

import com.Tiebreaker.dto.auth.MemberCreateRequest;
import com.Tiebreaker.dto.auth.LoginRequest;
import com.Tiebreaker.dto.auth.LoginResponse;
import com.Tiebreaker.dto.auth.MemberResponse;
import com.Tiebreaker.dto.auth.ErrorResponse;
import com.Tiebreaker.service.auth.MemberService;
import com.Tiebreaker.service.ImageService;
import com.Tiebreaker.exception.auth.LoginException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;
import org.springframework.core.io.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
@Validated
public class MemberController {

  private final MemberService memberService;
  private final ImageService imageService;

  // 회원가입
  @PostMapping("/join")
  public ResponseEntity<?> join(@Valid @ModelAttribute MemberCreateRequest request) {
    try {
      // 회원가입 처리
      MemberResponse memberResponse = memberService.join(request);
      
      return ResponseEntity.ok(memberResponse);
    } catch (Exception e) {
      log.error("회원가입 실패: {}", e.getMessage(), e);
      return ResponseEntity.badRequest().body(ErrorResponse.builder()
        .message(e.getMessage())
        .error("JOIN_FAILED")
        .status(400)
        .build());
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
  public ResponseEntity<MemberResponse> getCurrentMember() {
    MemberResponse memberResponse = memberService.getCurrentMember();
    return ResponseEntity.ok(memberResponse);
  }

  // 프로필 이미지 업데이트 (선택적 기능)
  @PutMapping("/profile-image")
  public ResponseEntity<?> updateProfileImage(@RequestParam("imageUrl") String imageUrl) {
    try {
      String updatedProfileImageUrl = memberService.updateProfileImage(imageUrl);
      return ResponseEntity.ok(Map.of(
        "message", "프로필 이미지가 업데이트되었습니다.",
        "profileImage", updatedProfileImageUrl
      ));
    } catch (Exception e) {
      return ResponseEntity.badRequest().body(Map.of(
        "error", "프로필 이미지 업데이트 실패",
        "message", e.getMessage()
      ));
    }
  }

  // 이미지 서빙 엔드포인트
  @GetMapping("/images/profile/{fileName:.+}")
  public ResponseEntity<Resource> serveProfileImage(@PathVariable String fileName) {
    try {
      Resource resource = imageService.loadProfileImageAsResource(fileName);
      return ResponseEntity.ok()
        .contentType(MediaType.IMAGE_JPEG)
        .body(resource);
    } catch (Exception e) {
      log.error("프로필 이미지 로드 실패: {}", fileName, e);
      return ResponseEntity.notFound().build();
    }
  }
  
  @GetMapping("/images/profile-default.svg")
  public ResponseEntity<Resource> serveDefaultProfileImage() {
    try {
      // WebConfig에서 정적 리소스로 설정된 경로 사용
      Resource resource = new org.springframework.core.io.ClassPathResource("static/images/default/profile-default.svg");
      return ResponseEntity.ok()
        .contentType(MediaType.valueOf("image/svg+xml"))
        .body(resource);
    } catch (Exception e) {
      log.error("기본 프로필 이미지 로드 실패", e);
      return ResponseEntity.notFound().build();
    }
  }
}
