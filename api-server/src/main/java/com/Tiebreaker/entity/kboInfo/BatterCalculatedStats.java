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
public class BatterCalculatedStats {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "player_id")
  private Player player;

  // 타율
  private Double battingAverage;
  // 장타율
  private Double sluggingPercentage;
  // 출루율
  private Double onBasePercentage;
  // 도루 성공률
  private Double stolenBasePercentage;
  // OPS
  private Double ops;
  // 득점권 타율
  private Double battingAverageWithRunnersInScoringPosition;
  // 대타 타율
  private Double pinchHitBattingAverage;

}
