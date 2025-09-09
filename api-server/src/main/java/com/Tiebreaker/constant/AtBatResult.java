package com.Tiebreaker.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AtBatResult {
  // --- 안타 (Hits) ---
  SINGLE("1루타", false, true, true),
  DOUBLE("2루타", false, true, true),
  TRIPLE("3루타", false, true, true),
  HOME_RUN("홈런", false, true, true),

  // --- 아웃 (Outs) ---
  STRIKEOUT("삼진", true, false, false),
  GROUND_OUT("땅볼 아웃", true, false, false),
  FLY_OUT("뜬공 아웃", true, false, false),
  LINE_DRIVE_OUT("직선타 아웃", true, false, false),
  SACRIFICE_FLY("희생플라이", true, false, true), // 아웃이지만, 타점/주자 진루 가능
  SACRIFICE_BUNT("희생번트", true, false, true), // 아웃이지만, 주자 진루 목적

  // --- 출루 (Reaches) - 안타 아님 ---
  WALK("볼넷 (BB)", false, false, true),
  INTENTIONAL_WALK("고의사구 (IBB)", false, false, true),
  HIT_BY_PITCH("몸에 맞는 공 (HBP)", false, false, true),
  FIELDER_S_CHOICE("야수선택", false, false, true), // 타자는 살지만, 다른 주자가 아웃
  DROPPED_THIRD_STRIKE("스트라이크아웃 낫아웃", false, false, true), // 낫아웃으로 출루
  CATCHER_INTERFERENCE("포수타격방해", false, false, true),

  // --- 기타 ---
  ERROR("수비 실책", false, false, true); // 수비 실책으로 출루

  private final String description; // 한글 설명
  private final boolean isOut;      // 아웃 여부
  private final boolean isHit;      // 안타 여부
  private final boolean isReach;    // 출루 여부 (타자가 1루에 도달했는가)

}
