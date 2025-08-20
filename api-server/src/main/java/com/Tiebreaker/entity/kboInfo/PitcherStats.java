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
public class PitcherStats {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "player_id")
  private Player player;

  private Integer year; // 연도 정보 (기본값: 현재 연도)

  // === 기본 기록 ===
  private Integer games; // 경기수
  private Integer wins; // 승리
  private Integer losses; // 패배
  private Integer saves; // 세이브
  private Integer blownSaves; // 블론세이브
  private Integer holds; // 홀드
  private Integer completeGames; // 완투
  private Integer shutouts; // 완봉
  private Integer totalBattersFaced; // 상대 타자 수
  private Integer numberOfPitches; // 투구 수
  private Integer inningsPitchedInteger; // 이닝
  private Integer inningsPitchedFraction; // 아웃카운트(0, 1/3, 2/3)
  private Integer hitsAllowed; // 허용 안타
  private Integer doublesAllowed; // 허용 2루타
  private Integer triplesAllowed; // 허용 3루타
  private Integer homeRunsAllowed; // 허용 홈런
  private Integer intentionalWalksAllowed; // 고의사구
  private Integer walksAllowed; // 볼넷
  private Integer runsAllowed; // 실점
  private Integer earnedRuns; // 자책점
  private Integer wildPitches; // 폭투
  private Integer balks; // 보크
  private Integer sacrificeHitsAllowed; // 허용 희생번트
  private Integer sacrificeFliesAllowed; // 허용 희생플라이
  private Integer strikeouts; // 삼진
  private Integer qualityStarts; // 퀄리티스타트

  // === 계산된 기록 ===
  private Double earnedRunAverage; // 평균자책점
  private Double winningPercentage; // 승률
  private Double whip; // WHIP
  private Double battingAverageAgainst; // 피안타율
}
