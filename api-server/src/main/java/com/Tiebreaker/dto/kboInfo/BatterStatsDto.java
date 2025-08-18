package com.Tiebreaker.dto.kboInfo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BatterStatsDto {

  // 연도 정보 (기본값: 현재 연도)
  private Integer year;

  // === 기본 기록 ===
  private Integer games;
  // 타석, 타수
  private Integer plateAppearances;
  private Integer atBats;
  // 안타, 2루타, 3루타, 홈런
  private Integer hits;
  private Integer doubles;
  private Integer triples;
  private Integer homeRuns;
  // 총 루타, 타점, 득점
  private Integer totalBases;
  private Integer runsBattedIn;
  private Integer runs;
  // 볼넷, 사구, 고의사구
  private Integer walks;
  private Integer hitByPitch;
  private Integer intentionalWalks;
  // 삼진, 병살타, 실책
  private Integer strikeouts;
  private Integer groundedIntoDoublePlay;
  private Integer errors;
  // 도루, 도루실패
  private Integer stolenBases;
  private Integer caughtStealing;
  // 희생번트, 희생플라이
  private Integer sacrificeHits;
  private Integer sacrificeFlies;

  // === 계산된 기록 ===
  // 타율
  private Double battingAverage;
  // 장타율
  private Double sluggingPercentage;
  // 출루율
  private Double onBasePercentage;
  // 도루 성공률
  private Double stolenBasePercentage;
  // OPS
  private Double ops;
  // 득점권 타율
  private Double battingAverageWithRunnersInScoringPosition;
  // 대타 타율
  private Double pinchHitBattingAverage;
}
