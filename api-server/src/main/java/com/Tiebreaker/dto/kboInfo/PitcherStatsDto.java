package com.Tiebreaker.dto.kboInfo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 투수의 통합 스탯 정보 DTO (기본 + 계산된 값)
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PitcherStatsDto {

  // 연도 정보 (기본값: 현재 연도)
  private Integer year;

  // === 기본 기록 ===
  private Integer games;
  // 승, 패, 세이브, 블론세이브, 홀드
  private Integer wins;
  private Integer losses;
  private Integer saves;
  private Integer blownSaves;
  private Integer holds;
  // 완투, 완봉
  private Integer completeGames;
  private Integer shutouts;
  // 상대 타자 수, 투구 수
  private Integer totalBattersFaced;
  private Integer numberOfPitches;
  // 이닝, 아웃카운트(0, 1/3, 2/3)
  private String inningsPitched; // 원본 문자열 (데이터 수집기에서 전송)
  private Integer inningsPitchedInteger; // 파싱된 정수 부분
  private Integer inningsPitchedFraction; // 파싱된 분수 부분
  // 허용 안타, 2루타, 3루타, 홈런
  private Integer hitsAllowed;
  private Integer doublesAllowed;
  private Integer triplesAllowed;
  private Integer homeRunsAllowed;
  // 고의사구, 볼넷, 실점, 자책점
  private Integer intentionalWalksAllowed;
  private Integer walksAllowed;
  private Integer runsAllowed;
  private Integer earnedRuns;
  // 폭투, 보크
  private Integer wildPitches;
  private Integer balks;
  // 허용 희생번트, 희생플라이
  private Integer sacrificeHitsAllowed;
  private Integer sacrificeFliesAllowed;
  // 삼진, 퀄리티스타트
  private Integer strikeouts;
  private Integer qualityStarts;

  // === 계산된 기록 ===
  // 평균자책점
  private Double earnedRunAverage;
  // 승률
  private Double winningPercentage;
  // WHIP
  private Double whip;
  // 피안타율
  private Double battingAverageAgainst;
}