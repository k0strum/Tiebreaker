package com.Tiebreaker.service.auth;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;

import lombok.RequiredArgsConstructor;
import com.Tiebreaker.dto.auth.MemberCreateRequest;
import com.Tiebreaker.dto.auth.MemberResponse;
import com.Tiebreaker.repository.MemberRepository;
import com.Tiebreaker.entity.auth.Member;

@Service
@RequiredArgsConstructor
public class MemberService {
  
  private final MemberRepository memberRepository;
  private final PasswordEncoder passwordEncoder;

  @Transactional
  public MemberResponse join(MemberCreateRequest request) {
    // 1. 입력값 검증
    validateJoinRequest(request);
    
    // 2. 중복 검사
    validateDuplicateMember(request);
    
    // 3. 회원 생성 및 저장
    Member member = createAndSaveMember(request);
    
    // 4. 회원 정보 반환
    return member.toResponse();
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
}