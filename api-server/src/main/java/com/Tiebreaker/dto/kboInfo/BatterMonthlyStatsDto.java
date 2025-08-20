package com.Tiebreaker.dto.kboInfo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BatterMonthlyStatsDto {

  private Integer year; // 연도
  private Integer month; // 월

  private Integer games; // 경기 수
  private Integer plateAppearances; // 타석
  private Integer atBats; // 타수
  private Integer hits; // 안타
  private Integer doubles; // 2루타
  private Integer triples; // 3루타
  private Integer homeRuns; // 홈런
  private Integer runsBattedIn; // 타점
  private Integer runs; // 득점
  private Integer walks; // 볼넷
  private Integer hitByPitch; // 사구
  private Integer strikeouts; // 삼진
  private Integer stolenBases; // 도루
  private Integer caughtStealing; // 도루 실패
  private Integer groundedIntoDoublePlay; // 병살타
}
