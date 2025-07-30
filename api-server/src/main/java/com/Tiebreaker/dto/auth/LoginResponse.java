package com.Tiebreaker.dto.auth;

import com.Tiebreaker.constant.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
  
  private String token;
  private Role role;
  private String profileImage;
  private String nickname;
  private Long memberId;
} 