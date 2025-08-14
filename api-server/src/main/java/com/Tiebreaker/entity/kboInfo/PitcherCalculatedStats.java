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
public class PitcherCalculatedStats {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @OneToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "player_id")
  private Player player;

  // 평균자책점
  private Double earnedRunAverage;
  // 승률
  private Double winningPercentage;
  // WHIP
  private Double whip;
  // 피안타율
  private Double battingAverageAgainst;

}
