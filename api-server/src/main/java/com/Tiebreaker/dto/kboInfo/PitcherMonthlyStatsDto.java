package com.Tiebreaker.dto.kboInfo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PitcherMonthlyStatsDto {

  private Integer year;
  private Integer month;

  private Integer games;
  private Integer inningsPitchedInteger;
  private Integer inningsPitchedFraction;
  private Integer strikeouts;
  private Integer runsAllowed;
  private Integer earnedRuns;
  private Integer hitsAllowed;
  private Integer homeRunsAllowed;
  private Integer totalBattersFaced;
  private Integer walksAllowed;
  private Integer hitByPitch;
  private Integer wins;
  private Integer losses;
  private Integer saves;
  private Integer holds;
}
