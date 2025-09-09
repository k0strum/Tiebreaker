package com.Tiebreaker.entity.scorebook;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class GameDetail {

  @Id
  private Long id;

  @OneToOne(fetch = FetchType.LAZY)
  @MapsId // Game.id를 GameDetail.id에 매핑
  @JoinColumn(name = "id")
  private Game game;

  @Column(nullable = false)
  private Integer homeScore;

  @Column(nullable = false)
  private Integer awayScore;

  private Integer homeHits;       // 홈팀 안타
  private Integer awayHits;       // 원정팀 안타
  private Integer homeErrors;     // 홈팀 에러
  private Integer awayErrors;     // 원정팀 에러
  private Integer homeWalks;      // 홈팀 볼넷 (BB)
  private Integer awayWalks;      // 원정팀 볼넷 (BB)
  private Integer homeHomeRuns;   // 홈팀 홈런
  private Integer awayHomeRuns;   // 원정팀 홈런

  // --- 승/패/세/홀 투수 정보 ---
  private String winningPitcher;
  private String losingPitcher;
  private String savePitcher;
  private String holdPitcher;

  // --- 주요 선수 기록 ---
  private String homeRunLeader; // 해당 경기 홈런 친 선수들 (JSON or String)
  private String rbiLeader;     // 해당 경기 타점 올린 선수들
  private String decisiveHitPlayer; // 결승타를 친 선수

  // --- 기타 정보 ---
  private String gameDuration; // 경기 소요 시간 (예: "3시간 21분")
  private String attendance;   // 관중 수

}
