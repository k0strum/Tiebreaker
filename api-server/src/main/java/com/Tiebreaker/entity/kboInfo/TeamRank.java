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
public class TeamRank {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "team_rank", nullable = false)
  private Integer rank;

  @Column(nullable = false, unique = true)
  private String teamName;

  @Column(nullable = false)
  private Double winRate;

  @Column(nullable = false)
  private Double gameBehind;

  @Column(nullable = false)
  private Integer wins;

  @Column(nullable = false)
  private Integer draws;

  @Column(nullable = false)
  private Integer losses;

  @Column(nullable = false)
  private Integer plays;

  @Column(nullable = false)
  private String streak;
}
