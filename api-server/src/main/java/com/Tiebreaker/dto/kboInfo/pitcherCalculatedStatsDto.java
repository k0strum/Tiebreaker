package com.Tiebreaker.dto.kboInfo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PitcherCalculatedStatsDto {
  // 평균자책점
  private Double earnedRunAverage;
  // 승률
  private Double winningPercentage;
  // WHIP
  private Double whip;
  // 피안타율
  private Double battingAverageAgainst;
}