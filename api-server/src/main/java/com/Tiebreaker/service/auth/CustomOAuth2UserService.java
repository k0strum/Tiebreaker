package com.Tiebreaker.service.auth;

import com.Tiebreaker.entity.auth.Member;
import com.Tiebreaker.repository.MemberRepository;
import com.Tiebreaker.constant.Role;
import com.Tiebreaker.service.ImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

  private final MemberRepository memberRepository;
  private final ImageService imageService;

  @Override
  public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
    OAuth2User oAuth2User = new DefaultOAuth2UserService().loadUser(userRequest);

    String registrationId = userRequest.getClientRegistration().getRegistrationId();
    String userNameAttributeName = userRequest.getClientRegistration().getProviderDetails()
        .getUserInfoEndpoint().getUserNameAttributeName();

    Map<String, Object> attributes = oAuth2User.getAttributes();
    String email = extractEmail(registrationId, attributes);
    String name = extractName(registrationId, attributes);
    String originalProfileImageUrl = extractProfileImage(registrationId, attributes);

    // 필수 필드 검증
    if (email == null || email.trim().isEmpty()) {
      throw new OAuth2AuthenticationException(
          new OAuth2Error("invalid_email", "이메일 정보를 가져올 수 없습니다.", null));
    }

    if (name == null || name.trim().isEmpty()) {
      name = "사용자"; // 기본값 설정
    }

    // 디버깅을 위한 로그
    System.out.println("=== 소셜 로그인 정보 추출 ===");
    System.out.println("Provider: " + registrationId);
    System.out.println("Email: " + email);
    System.out.println("Name: " + name);
    System.out.println("Profile Image: " + originalProfileImageUrl);
    System.out.println("================================");

    // 기존 회원인지 확인
    Optional<Member> existingMember = memberRepository.findByEmail(email);

    if (existingMember.isPresent()) {
      Member member = existingMember.get();

      // 기존 회원의 로그인 타입 확인
      String existingLoginType = member.getLoginType();

      if (existingLoginType == null || "LOCAL".equals(existingLoginType)) {
        // 일반 가입자(로컬 회원)라면 소셜 로그인 차단
        throw new OAuth2AuthenticationException(
            new OAuth2Error("email_exists", "이미 해당 이메일로 가입된 계정이 있습니다. 일반 로그인을 이용해 주세요.", null));
      }

      // 같은 소셜 로그인 타입이거나 다른 소셜 로그인 타입인 경우 허용
      // (소셜 로그인 사용자는 다른 소셜 로그인으로도 로그인 가능)

      // 기존 회원의 경우 프로필 이미지는 유지 (최초 회원가입 시의 이미지 사용)
      System.out.println("기존 회원 로그인 - 프로필 이미지 유지: " + member.getProfileImage());

      return new DefaultOAuth2User(
          Collections.singleton(new SimpleGrantedAuthority(member.getRole().toString())),
          attributes,
          userNameAttributeName);
    } else {
      // 새 회원인 경우 회원가입 처리
      System.out.println("=== 새 회원 소셜 회원가입 ===");
      Member newMember = new Member();

      // 프로필 이미지 처리 (최초 회원가입 시에만 다운로드)
      String profileImageName = null;
      if (originalProfileImageUrl != null && !originalProfileImageUrl.trim().isEmpty()) {
        System.out.println("프로필 이미지 다운로드 시작: " + originalProfileImageUrl);
        String savedProfileImgPath = imageService.downloadAndSaveProfileImage(originalProfileImageUrl);
        System.out.println("프로필 이미지 다운로드 완료: " + savedProfileImgPath);

        // 웹 경로에서 파일명만 추출
        if (savedProfileImgPath != null && savedProfileImgPath.startsWith("/api/members/images/profile/")) {
          profileImageName = savedProfileImgPath.replace("/api/members/images/profile/", "");
        }
      } else {
        System.out.println("프로필 이미지 URL이 없어 기본 이미지 사용");
        profileImageName = "profile-default.svg";
      }

      System.out.println("소셜 로그인 attributes: " + attributes);
      System.out.println("저장할 프로필 이미지 파일명: " + profileImageName);
      newMember.setProfileImage(profileImageName);
      newMember.setEmail(email);
      newMember.setNickname(name != null ? name : "사용자");
      newMember.setRole(Role.USER);
      newMember.setPassword("SOCIAL_LOGIN"); // 소셜 로그인이므로 비밀번호는 더미값으로 설정
      newMember.setPhone(null); // 전화번호는 나중에 마이페이지에서 입력
      newMember.setMileage(0);
      newMember.setLoginType(registrationId.toUpperCase()); // GOOGLE, KAKAO, NAVER
      newMember.setEmailVerified(true); // 소셜 로그인은 이메일 인증 완료로 간주
      newMember.setSocialId(extractSocialId(registrationId, attributes));

      memberRepository.save(newMember);

      // 임시 OAuth2User 반환 (실제로는 인증 성공 핸들러에서 처리됨)
      return new DefaultOAuth2User(
          Collections.singleton(new SimpleGrantedAuthority(Role.USER.toString())),
          attributes,
          userNameAttributeName);
    }
  }

  public String extractProvider(Map<String, Object> attributes) {
    if (attributes.containsKey("response"))
      return "naver";
    if (attributes.containsKey("kakao_account"))
      return "kakao";
    return "google";
  }

  public String extractEmail(String provider, Map<String, Object> attributes) {
    if ("naver".equals(provider)) {
      Map<String, Object> response = (Map<String, Object>) attributes.get("response");
      System.out.println(response.toString());
      return (String) response.get("email");
    }
    if ("kakao".equals(provider)) {
      Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
      return (String) kakaoAccount.get("email");
    }
    // 기본: 구글 등 기타
    return (String) attributes.get("email");
  }

  public String extractName(String provider, Map<String, Object> attributes) {
    if ("naver".equals(provider)) {
      Map<String, Object> response = (Map<String, Object>) attributes.get("response");

      // 디버깅을 위한 로그 추가
      System.out.println("=== 네이버 닉네임 추출 디버깅 ===");
      System.out.println("Response: " + response);
      System.out.println("Nickname from response: " + response.get("nickname"));
      System.out.println("Name from response: " + response.get("name"));
      System.out.println("Email from response: " + response.get("email"));
      System.out.println("=================================");

      // 네이버는 nickname 필드를 사용
      String nickname = (String) response.get("nickname");
      if (nickname != null && !nickname.trim().isEmpty()) {
        System.out.println("네이버 닉네임 사용: " + nickname);
        return nickname;
      }
      // nickname이 없으면 name 필드 사용
      String name = (String) response.get("name");
      if (name != null && !name.trim().isEmpty()) {
        System.out.println("네이버 이름 사용: " + name);
        return name;
      }
      // 둘 다 없으면 이메일에서 @ 앞부분 사용
      String email = (String) response.get("email");
      if (email != null && email.contains("@")) {
        String emailPrefix = email.substring(0, email.indexOf("@"));
        System.out.println("네이버 이메일 접두사 사용: " + emailPrefix);
        return emailPrefix;
      }
      // 마지막 대안으로 "네이버 사용자" 반환
      System.out.println("네이버 기본값 사용: 네이버 사용자");
      return "네이버 사용자";
    }
    if ("kakao".equals(provider)) {
      Map<String, Object> properties = (Map<String, Object>) attributes.get("properties");
      String nickname = (String) properties.get("nickname");
      if (nickname != null && !nickname.trim().isEmpty()) {
        return nickname;
      }
      // 카카오도 이메일에서 @ 앞부분 사용
      Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
      String email = (String) kakaoAccount.get("email");
      if (email != null && email.contains("@")) {
        return email.substring(0, email.indexOf("@"));
      }
      return "카카오 사용자";
    }
    // 기본: 구글 등 기타
    String name = (String) attributes.get("name");
    if (name != null && !name.trim().isEmpty()) {
      return name;
    }
    // 구글도 이메일에서 @ 앞부분 사용
    String email = (String) attributes.get("email");
    if (email != null && email.contains("@")) {
      return email.substring(0, email.indexOf("@"));
    }
    return "사용자";
  }

  public String extractProfileImage(String provider, Map<String, Object> attributes) {
    if ("naver".equals(provider)) {
      Map<String, Object> naverResponse = (Map<String, Object>) attributes.get("response");
      return (String) naverResponse.get("profile_image");
    }
    if ("kakao".equals(provider)) {
      Map<String, Object> kakaoProperties = (Map<String, Object>) attributes.get("properties");
      return (String) kakaoProperties.get("profile_image");
    }
    // 기본: 구글 등 기타
    return (String) attributes.get("picture");
  }

  public String extractSocialId(String provider, Map<String, Object> attributes) {
    if ("naver".equals(provider)) {
      Map<String, Object> response = (Map<String, Object>) attributes.get("response");
      return (String) response.get("id");
    }
    if ("kakao".equals(provider)) {
      return attributes.get("id").toString();
    }
    // 기본: 구글 등 기타
    return (String) attributes.get("sub");
  }

  private String extractFileNameFromUrl(String url) {
    if (url == null || url.isEmpty()) {
      return null;
    }

    // URL에서 파일명만 추출 (마지막 / 이후 부분)
    String[] parts = url.split("/");
    String fileName = parts[parts.length - 1];

    // URL 파라미터가 있는 경우 제거
    if (fileName.contains("?")) {
      fileName = fileName.substring(0, fileName.indexOf("?"));
    }

    return fileName;
  }
}
