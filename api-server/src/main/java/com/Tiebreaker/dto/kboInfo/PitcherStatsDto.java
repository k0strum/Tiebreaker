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

  // 승, 패, 세이브, 블론세이브, 홀드
  private Integer win;
  private Integer lose;
  private Integer save;
  private Integer blownSave;
  private Integer hold;
  // 완투, 완봉
  private Integer completeGame;
  private Integer shutout;
  // 상대 타자 수, 투구 수, 이닝
  private Integer batterFaced;
  private Integer pitchCount;
  private Double innings; // 1/3, 2/3 이닝이 있으므로 Double 타입이 더 적합
  // 허용 안타, 2루타, 3루타, 홈런
  private Integer hits;
  private Integer doubles;
  private Integer triples;
  private Integer homeRuns;
  // 볼넷, 실점, 자책점
  private Integer baseOnBalls;
  private Integer runs;
  private Integer earnedRuns;
  // 삼진, 퀄리티스타트
  private Integer strikeOut;
  private Integer qualityStart;
}