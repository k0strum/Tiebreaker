package com.Tiebreaker.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberCreateRequest {
  
  @NotBlank(message = "이메일은 필수입니다.")
  @Email(message = "올바른 이메일 형식이 아닙니다.")
  private String email;
  
  @NotBlank(message = "비밀번호는 필수입니다.")
  @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$", 
           message = "비밀번호는 8자 이상이며, 영문, 숫자, 특수문자를 포함해야 합니다.")
  private String password;
  
  @NotBlank(message = "닉네임은 필수입니다.")
  private String nickname;
  
  @Pattern(regexp = "^01[0-9]-?[0-9]{4}-?[0-9]{4}$", 
           message = "올바른 휴대폰 번호 형식이 아닙니다.")
  private String phone;
  
  private String address;
  
  private String profileImageUrl; // 소셜 로그인 시 프로필 이미지 URL
  
  private MultipartFile profileImage; // 로컬 회원가입 시 프로필 이미지 파일
} 