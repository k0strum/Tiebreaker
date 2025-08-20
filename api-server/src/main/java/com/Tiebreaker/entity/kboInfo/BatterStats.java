package com.Tiebreaker.entity.kboInfo;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BatterStats {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "player_id")
  private Player player;

  private Integer year; // 연도 정보 (기본값: 현재 연도)

  // === 기본 기록 ===
  private Integer games; // 경기수
  private Integer plateAppearances; // 타석
  private Integer atBats; // 타수
  private Integer hits; // 안타
  private Integer doubles; // 2루타
  private Integer triples; // 3루타
  private Integer homeRuns; // 홈런
  private Integer totalBases; // 총 루타
  private Integer runsBattedIn; // 타점
  private Integer runs; // 득점
  private Integer walks; // 볼넷
  private Integer hitByPitch; // 사구
  private Integer intentionalWalks; // 고의사구
  private Integer strikeouts; // 삼진
  private Integer groundedIntoDoublePlay; // 병살타
  private Integer errors; // 실책
  private Integer stolenBases; // 도루
  private Integer caughtStealing; // 도루실패
  private Integer sacrificeHits; // 희생번트
  private Integer sacrificeFlies; // 희생플라이

  // === 계산된 기록 ===
  private Double battingAverage; // 타율
  private Double sluggingPercentage; // 장타율
  private Double onBasePercentage; // 출루율
  private Double stolenBasePercentage; // 도루 성공률
  private Double ops; // OPS
  private Double battingAverageWithRunnersInScoringPosition; // 득점권 타율
  private Double pinchHitBattingAverage; // 대타 타율
}
