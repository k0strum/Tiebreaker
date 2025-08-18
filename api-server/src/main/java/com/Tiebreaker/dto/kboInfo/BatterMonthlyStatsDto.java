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

  private Integer year;
  private Integer month;

  private Integer games;
  private Integer plateAppearances;
  private Integer atBats;
  private Integer hits;
  private Integer doubles;
  private Integer triples;
  private Integer homeRuns;
  private Integer runsBattedIn;
  private Integer runs;
  private Integer walks;
  private Integer hitByPitch;
  private Integer strikeouts;
  private Integer stolenBases;
  private Integer caughtStealing;
  private Integer groundedIntoDoublePlay;
}
