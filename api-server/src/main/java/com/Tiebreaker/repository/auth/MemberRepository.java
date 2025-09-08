package com.Tiebreaker.repository.auth;

import com.Tiebreaker.entity.auth.Member;
import com.Tiebreaker.constant.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {
  
  // 이메일로 회원 찾기 (로그인용)
  Optional<Member> findByEmail(String email);
  
  // 이메일 존재 여부 확인 (회원가입 중복 체크용)
  boolean existsByEmail(String email);
  
  // 닉네임으로 회원 찾기
  Optional<Member> findByNickname(String nickname);
  
  // 닉네임 존재 여부 확인 (회원가입 중복 체크용)
  boolean existsByNickname(String nickname);
  
  // 이메일과 비밀번호로 회원 찾기 (로그인 검증용)
  Optional<Member> findByEmailAndPassword(String email, String password);
  
  // 전화번호로 회원 찾기
  Optional<Member> findByPhone(String phone);
  
  // 전화번호 존재 여부 확인
  boolean existsByPhone(String phone);
  
  // 역할별 회원 목록 찾기
  List<Member> findByRole(Role role);
  
  // 이메일이 포함된 회원 찾기 (검색용)
  List<Member> findByEmailContaining(String email);
  
  // 닉네임이 포함된 회원 찾기 (검색용)
  List<Member> findByNicknameContaining(String nickname);
}
