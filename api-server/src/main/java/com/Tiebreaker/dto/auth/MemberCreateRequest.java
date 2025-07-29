package com.Tiebreaker.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberCreateRequest {
  
  private String email;
  private String password;
  private String nickname;
  private String phone;
  private String address;
} 