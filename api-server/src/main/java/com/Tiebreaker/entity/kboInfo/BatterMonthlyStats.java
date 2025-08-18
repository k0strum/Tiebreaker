package com.Tiebreaker.entity.kboInfo;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(uniqueConstraints = {
    @UniqueConstraint(name = "uk_batter_monthly_player_year_month", columnNames = { "player_id", "year", "month" })
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BatterMonthlyStats {

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
  private Integer plateAppearances; // 타석
  private Integer atBats; // 타수
  private Integer hits; // 안타
  private Integer doubles; // 2루타
  private Integer triples; // 3루타
  private Integer homeRuns; // 홈런
  private Integer runsBattedIn; // 타점
  private Integer runs; // 득점
  private Integer walks; // 볼넷
  private Integer hitByPitch; // 사구
  private Integer strikeouts; // 삼진
  private Integer stolenBases; // 도루
  private Integer caughtStealing; // 도루실패
  private Integer groundedIntoDoublePlay; // 병살타
}
