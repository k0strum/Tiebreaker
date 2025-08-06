package com.Tiebreaker.service.auth;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;

import lombok.RequiredArgsConstructor;
import com.Tiebreaker.dto.auth.MemberCreateRequest;
import com.Tiebreaker.dto.auth.MemberResponse;
import com.Tiebreaker.dto.auth.LoginRequest;
import com.Tiebreaker.dto.auth.LoginResponse;
import com.Tiebreaker.repository.MemberRepository;
import com.Tiebreaker.entity.auth.Member;
import com.Tiebreaker.config.JwtTokenProvider;
import com.Tiebreaker.constant.VerificationType;
import com.Tiebreaker.exception.auth.LoginException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

@Service
@RequiredArgsConstructor
public class MemberService {
  
  private final MemberRepository memberRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtTokenProvider jwtTokenProvider;
  private final UserDetailsService userDetailsService;
  private final EmailVerificationService emailVerificationService;

  @Transactional
  public MemberResponse join(MemberCreateRequest request) {
    // 1. 입력값 검증
    validateJoinRequest(request);
    
    // 2. 중복 검사
    validateDuplicateMember(request);
    
    // 3. 회원 생성 및 저장
    Member member = createAndSaveMember(request);
    
    // 4. 이메일 인증 토큰 생성 및 발송
    emailVerificationService.createVerificationToken(
      member.getEmail(),
      member.getId(),
      VerificationType.SIGNUP,
      member.getNickname()
    );
    
    // 5. 회원 정보 반환
    return member.toResponse();
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
      .profileImage(member.getProfileImage() != null ? member.getProfileImage() : "")
      .nickname(member.getNickname())
      .memberId(member.getId())
      .build();
  }

  // 입력값 검증
  private void validateJoinRequest(MemberCreateRequest request) {
    if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
      throw new IllegalArgumentException("이메일은 필수입니다.");
    }
    if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
      throw new IllegalArgumentException("비밀번호는 필수입니다.");
    }
    if (request.getNickname() == null || request.getNickname().trim().isEmpty()) {
      throw new IllegalArgumentException("닉네임은 필수입니다.");
    }
    
    // 이메일 형식 검증
    if (!isValidEmail(request.getEmail())) {
      throw new IllegalArgumentException("올바른 이메일 형식이 아닙니다.");
    }
    
    // 비밀번호 길이 검증
    if (request.getPassword().length() < 6) {
      throw new IllegalArgumentException("비밀번호는 6자 이상이어야 합니다.");
    }
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
    // TODO: 이미지 저장 로직 추가 예정
    String profileImageName = "default.jpg";
    
    // 회원 생성
    Member member = Member.from(request, passwordEncoder, profileImageName);
    
    // 회원 저장
    return memberRepository.save(member);
  }

  // 이메일 형식 검증
  private boolean isValidEmail(String email) {
    String emailRegex = "^[A-Za-z0-9+_.-]+@(.+)$";
    return email.matches(emailRegex);
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
}