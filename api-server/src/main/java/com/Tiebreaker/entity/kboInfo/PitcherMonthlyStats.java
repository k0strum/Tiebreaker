package com.Tiebreaker.entity.kboInfo;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(uniqueConstraints = {
    @UniqueConstraint(name = "uk_pitcher_monthly_player_year_month", columnNames = { "player_id", "year", "month" })
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PitcherMonthlyStats {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "player_id")
  private Player player;

  private Integer year;
  private Integer month;

  // === 기본 기록 ===
  private Integer games; // 경기수
  // 이닝, 아웃카운트(0, 1/3, 2/3)
  private Integer inningsPitchedInteger;
  private Integer inningsPitchedFraction;
  private Integer strikeouts; // 삼진
  private Integer runsAllowed; // 실점
  private Integer earnedRuns; // 자책점
  private Integer hitsAllowed; // 피안타
  private Integer homeRunsAllowed; // 피홈런
  private Integer totalBattersFaced; // 타자수
  private Integer walksAllowed; // 볼넷
  private Integer hitByPitch; // 사구
  private Integer wins, losses; // 승수, 패수
  private Integer saves, holds; // 세이브, 홀드
}