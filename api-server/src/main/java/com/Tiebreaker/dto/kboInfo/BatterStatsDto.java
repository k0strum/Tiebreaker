package com.Tiebreaker.dto.kboInfo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 타자의 원본 스탯 정보 DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BatterStatsDto {

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
  private Integer baseOnBalls;
  private Integer hitByPitch;
  private Integer intentionalBasesOnBalls;
  // 삼진, 병살타, 실책
  private Integer strikeOuts;
  private Integer groundIntoDoublePlay;
  private Integer errors;
  // 도루, 도루실패
  private Integer stolenBases;
  private Integer caughtStealing;
  // 희생번트, 희생플라이
  private Integer sacrifices;
  private Integer sacrificeFlies;
}