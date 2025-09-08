package com.Tiebreaker.service.auth;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;

import lombok.RequiredArgsConstructor;
import com.Tiebreaker.dto.auth.MemberCreateRequest;
import com.Tiebreaker.dto.auth.MemberResponse;
import com.Tiebreaker.dto.auth.LoginRequest;
import com.Tiebreaker.dto.auth.LoginResponse;
import com.Tiebreaker.repository.auth.MemberRepository;
import com.Tiebreaker.entity.auth.Member;
import com.Tiebreaker.config.JwtTokenProvider;
import com.Tiebreaker.constant.VerificationType;
import com.Tiebreaker.exception.auth.LoginException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import com.Tiebreaker.service.ImageService;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class MemberService {
  
  private final MemberRepository memberRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtTokenProvider jwtTokenProvider;
  private final UserDetailsService userDetailsService;
  private final EmailVerificationService emailVerificationService;
  private final ImageService imageService;

  @Transactional
  public MemberResponse join(MemberCreateRequest request) {
    // 1. 중복 검사 (비즈니스 로직)
    validateDuplicateMember(request);
    
    // 2. 회원 생성 및 저장
    Member member = createAndSaveMember(request);
    
    // 3. 이메일 인증 토큰 생성 및 발송
    emailVerificationService.createVerificationToken(
      member.getEmail(),
      member.getId(),
      VerificationType.SIGNUP,
      member.getNickname()
    );
    
    // 4. 회원 정보 반환
    return member.toResponse();
  }

  @Transactional
  public MemberResponse join(MemberCreateRequest request, MultipartFile profileImage) {
    // 기존 오버로드 메서드 유지 (하위 호환성)
    request.setProfileImage(profileImage);
    return join(request);
  }

  @Transactional(readOnly = true)
  public LoginResponse login(LoginRequest request) {
    System.out.println("=== 로그인 시도: " + request.getEmail() + " ===");
    
    // 1. 회원 존재 여부 확인 (보안상 구체적인 정보 제공 안함)
    Member member = memberRepository.findByEmail(request.getEmail())
      .orElse(null);
    
    // 2. 회원이 존재하지 않는 경우 (보안상 구체적인 정보 제공 안함)
    if (member == null) {
      System.out.println("회원이 존재하지 않음");
      throw new LoginException(LoginException.LoginErrorType.INVALID_CREDENTIALS, "이메일 또는 비밀번호가 올바르지 않습니다.");
    }
    
    System.out.println("회원 정보: " + member.getEmail() + ", 로그인 타입: " + member.getLoginType() + ", 이메일 인증: " + member.isEmailVerified());
    
    // 3. 소셜 로그인 회원인지 확인
    if (!"LOCAL".equals(member.getLoginType())) {
      System.out.println("소셜 로그인 회원");
      throw new LoginException(LoginException.LoginErrorType.SOCIAL_LOGIN_REQUIRED, "소셜 로그인으로 가입된 계정입니다. 소셜 로그인을 이용해 주세요.");
    }
    
    // 4. 이메일 인증 확인 (로컬 로그인의 경우)
    if ("LOCAL".equals(member.getLoginType()) && !member.isEmailVerified()) {
      System.out.println("이메일 미인증");
      throw new LoginException(LoginException.LoginErrorType.EMAIL_NOT_VERIFIED, "이메일 인증이 완료되지 않았습니다. 이메일을 확인하여 인증을 완료해주세요.");
    }
    
    // 5. UserDetails 로드
    UserDetails userDetails = userDetailsService.loadUserByUsername(request.getEmail());
    
    // 6. 비밀번호 검증 (보안상 구체적인 정보 제공 안함)
    if (!passwordEncoder.matches(request.getPassword(), userDetails.getPassword())) {
      System.out.println("비밀번호 불일치");
      throw new LoginException(LoginException.LoginErrorType.INVALID_CREDENTIALS, "이메일 또는 비밀번호가 올바르지 않습니다.");
    }
    
    // 6. JWT 토큰 생성
    String token = jwtTokenProvider.createToken(
      userDetails.getUsername(), 
      member.getRole().toString()
    );
    
    // 7. 로그인 응답 생성
    return LoginResponse.builder()
      .token(token)
      .role(member.getRole())
      .profileImage(imageService.getProfileImageUrl(member.getProfileImage()))
      .nickname(member.getNickname())
      .memberId(member.getId())
      .build();
  }

  // 중복 검사
  private void validateDuplicateMember(MemberCreateRequest request) {
    if (memberRepository.existsByEmail(request.getEmail())) {
      throw new IllegalStateException("이미 존재하는 이메일입니다.");
    }
    if (memberRepository.existsByNickname(request.getNickname())) {
      throw new IllegalStateException("이미 존재하는 닉네임입니다.");
    }
    if (request.getPhone() != null && memberRepository.existsByPhone(request.getPhone())) {
      throw new IllegalStateException("이미 존재하는 전화번호입니다.");
    }
  }



  // 회원 생성 및 저장
  private Member createAndSaveMember(MemberCreateRequest request) {
    String profileImageUrl = null;
    
    // MultipartFile이 전달된 경우 파일을 다운로드하여 저장
    if (request.getProfileImage() != null && !request.getProfileImage().isEmpty()) {
      profileImageUrl = imageService.uploadProfileImage(request.getProfileImage());
    } else if (request.getProfileImageUrl() != null && !request.getProfileImageUrl().trim().isEmpty()) {
      // URL로 전달된 경우 다운로드하여 저장
      profileImageUrl = imageService.downloadAndSaveProfileImage(request.getProfileImageUrl());
    } else {
      // 프로필 이미지가 없는 경우 기본 이미지 사용
      profileImageUrl = "/api/members/images/profile-default.svg";
    }
    
    // 회원 생성 (파일명만 저장)
    String profileImageName = extractFileNameFromUrl(profileImageUrl);
    Member member = Member.from(request, passwordEncoder, profileImageName);
    
    // 회원 저장
    return memberRepository.save(member);
  }
  
  // URL에서 파일명 추출
  private String extractFileNameFromUrl(String url) {
    if (url == null || url.trim().isEmpty()) {
      return "profile-default.svg";
    }
    
    if (url.startsWith("/api/members/images/profile/")) {
      return url.replace("/api/members/images/profile/", "");
    }
    
    if (url.equals("/api/members/images/profile-default.svg")) {
      return "profile-default.svg";
    }
    
    return url;
  }

  // 회원 조회 (추가 메서드)
  @Transactional(readOnly = true)
  public MemberResponse findById(Long id) {
    Member member = memberRepository.findById(id)
      .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));
    
    return member.toResponse();
  }

  // 이메일로 회원 조회 (추가 메서드)
  @Transactional(readOnly = true)
  public MemberResponse findByEmail(String email) {
    Member member = memberRepository.findByEmail(email)
      .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));
    
    return member.toResponse();
  }

  // 현재 로그인한 사용자 정보 조회
  @Transactional(readOnly = true)
  public MemberResponse getCurrentMember() {
    // SecurityContext에서 현재 인증된 사용자의 이메일을 가져옴
    String email = org.springframework.security.core.context.SecurityContextHolder
      .getContext()
      .getAuthentication()
      .getName();
    
    Member member = memberRepository.findByEmail(email)
      .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));
    
    return member.toResponse();
  }

  // 프로필 이미지 업데이트 (선택적 기능)
  @Transactional
  public String updateProfileImage(String imageUrl) {
    // SecurityContext에서 현재 인증된 사용자의 이메일을 가져옴
    String email = org.springframework.security.core.context.SecurityContextHolder
      .getContext()
      .getAuthentication()
      .getName();
    
    Member member = memberRepository.findByEmail(email)
      .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));
    
    // 새 프로필 이미지 다운로드 및 저장
    String newProfileImageUrl = imageService.downloadAndSaveProfileImage(imageUrl);
    if (newProfileImageUrl == null) {
      throw new IllegalArgumentException("프로필 이미지 다운로드에 실패했습니다.");
    }
    
    // 파일명만 저장
    String newProfileImageName = extractFileNameFromUrl(newProfileImageUrl);
    member.setProfileImage(newProfileImageName);
    
    // 회원 정보 저장
    memberRepository.save(member);
    
    System.out.println("프로필 이미지 업데이트 완료: " + newProfileImageName);
    
    return newProfileImageUrl;
  }
}