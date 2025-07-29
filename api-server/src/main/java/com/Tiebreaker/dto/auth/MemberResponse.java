package com.Tiebreaker.dto.auth;

import com.Tiebreaker.constant.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberResponse {
  
  private Long id;
  private String email;
  private String nickname;
  private Role role;
  private String phone;
  private String address;
  private String profileImage;
  private int mileage;
  private LocalDateTime regDate;
  private LocalDateTime updateDate;
} 