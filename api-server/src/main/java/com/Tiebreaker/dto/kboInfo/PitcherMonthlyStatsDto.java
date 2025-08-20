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

  private Integer year; // 연도
  private Integer month; // 월

  private Integer games; // 경기 수
  private Integer inningsPitchedInteger; // 이닝 정수 부분
  private Integer inningsPitchedFraction; // 이닝 분수 부분
  private Integer strikeouts; // 삼진
  private Integer runsAllowed; // 실점
  private Integer earnedRuns; // 자책점
  private Integer hitsAllowed; // 허용 안타
  private Integer homeRunsAllowed; // 허용 홈런
  private Integer totalBattersFaced; // 상대 타자 수
  private Integer walksAllowed; // 볼넷
  private Integer hitByPitch; // 사구
  private Integer wins; // 승
  private Integer losses; // 패
  private Integer saves; // 세이브
  private Integer holds; // 홀드
}
