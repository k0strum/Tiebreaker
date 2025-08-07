package com.Tiebreaker.entity.auth;

import com.Tiebreaker.constant.Role;
import com.Tiebreaker.dto.auth.MemberCreateRequest;
import com.Tiebreaker.dto.auth.MemberResponse;
import com.Tiebreaker.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.crypto.password.PasswordEncoder;

@Entity
@Table(name = "member")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Member extends BaseTimeEntity{

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id", nullable = false)
  private Long id;

  @Column(name = "email", nullable = false, unique = true)
  private String email;

  @Column(name = "password", nullable = false)
  private String password;

  @Column(name = "nickname", nullable = false)
  private String nickname;

  @Enumerated(EnumType.STRING)
  @Column(name = "role", nullable = false)
  private Role role;

  @Column(name = "phone")
  private String phone;

  @Column(name = "address")
  private String address;

  @Column(name = "profile_image", length = 500)
  private String profileImage;

  @Column(name = "mileage")
  private int mileage;

  @Column(name = "login_type")
  private String loginType; // "LOCAL", "KAKAO", "GOOGLE", "NAVER"

  @Column(name = "social_id")
  private String socialId; // 소셜 로그인 ID

  @Column(name = "email_verified", nullable = false)
  private boolean emailVerified = false; // 이메일 인증 완료 여부

  // 이메일 인증 완료 여부 확인 메서드
  public boolean isEmailVerified() {
    return this.emailVerified;
  }

  // DTO를 사용한 정적 팩토리 메서드(회원 정보 생성 시 사용)
  public static Member from(MemberCreateRequest request,
                            PasswordEncoder passwordEncoder,
                            String profileImageName) {
    Member member = new Member();
    member.setEmail(request.getEmail());
    member.setPassword(passwordEncoder.encode(request.getPassword()));
    member.setNickname(request.getNickname());
    member.setRole(Role.USER);
    member.setPhone(request.getPhone());
    member.setAddress(request.getAddress());
    member.setProfileImage(profileImageName);
    member.setMileage(0);
    member.setLoginType("LOCAL");
    member.setEmailVerified(false); // 회원가입 시 이메일 미인증 상태
    return member;
  }

  // DTO로 변환하는 메서드(회원 정보 조회 시 사용)
  public MemberResponse toResponse() {
    // 프로필 이미지 URL 처리
    String profileImageUrl = null;
    if (this.profileImage != null && !this.profileImage.trim().isEmpty()) {
      if ("profile-default.svg".equals(this.profileImage)) {
        profileImageUrl = "/api/members/images/profile-default.svg";
      } else {
        profileImageUrl = "/api/members/images/profile/" + this.profileImage;
      }
    }
    
    return MemberResponse.builder()
      .id(this.id)
      .email(this.email)
      .nickname(this.nickname)
      .role(this.role)
      .phone(this.phone)
      .address(this.address)
      .profileImage(profileImageUrl)
      .mileage(this.mileage)
      .regDate(this.getRegDate())
      .updateDate(this.getUpdateDate())
      .build();
  }

}