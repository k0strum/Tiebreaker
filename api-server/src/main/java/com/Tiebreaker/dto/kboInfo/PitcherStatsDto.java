package com.Tiebreaker.dto.kboInfo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 투수의 원본 스탯 정보 DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PitcherStatsDto {

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
  // 상대 타자 수, 투구 수, 이닝
  private Integer totalBattersFaced;
  private Integer numberOfPitches;
  private String inningsPitched; // "23 2/3", "120", "1/3" 등의 형태로 저장
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
}