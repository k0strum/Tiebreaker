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

  private Integer games;
  // 승리 / 패배 / 세이브 / 블론세이브 / 홀드
  private Integer wins;
  private Integer losses;
  private Integer saves;
  private Integer blownSaves;
  private Integer holds;
  // 완투 / 완봉
  private Integer completeGames;
  private Integer shutouts;
  // 상대 타자 수, 투구 수, 이닝
  private Integer totalBattersFaced;
  private Integer numberOfPitches;
  private Double inningsPitched;
  // 허용 안타 / 2루타 / 3루타 / 홈런
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
