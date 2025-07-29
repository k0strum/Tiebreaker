package com.Tiebreaker.entity.auth;

import com.Tiebreaker.constant.Role;
import com.Tiebreaker.dto.auth.MemberCreateRequestDto;
import com.Tiebreaker.dto.auth.MemberResponseDto;
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

  @Column(name = "profile_image")
  private String profileImage;

  @Column(name = "mileage")
  private int mileage;


  // DTO를 사용한 정적 팩토리 메서드(회원 정보 생성 시 사용)
  public static Member from(MemberCreateRequestDto request,
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
    return member;
  }

  // DTO로 변환하는 메서드(회원 정보 조회 시 사용)
  public MemberResponseDto toResponse() {
    return MemberResponseDto.builder()
      .id(this.id)
      .email(this.email)
      .nickname(this.nickname)
      .role(this.role)
      .phone(this.phone)
      .address(this.address)
      .profileImage(this.profileImage)
      .mileage(this.mileage)
      .regDate(this.getRegDate())
      .updateDate(this.getUpdateDate())
      .build();
  }

}