package com.Tiebreaker.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "live_game_info")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LiveGameInfo extends BaseTimeEntity {

  @Id
  @Column(name = "game_id", length = 32)
  private String gameId;

  @Column(name = "status", length = 16)
  private String status; // READY, LIVE, FINISHED

  @Column(name = "inning")
  private Integer inning;

  @Column(name = "half", length = 1)
  private String half; // T (초), B (말)

  @Column(name = "home_score")
  private Integer homeScore;

  @Column(name = "away_score")
  private Integer awayScore;

  @Column(name = "home_team", length = 16)
  private String homeTeam;

  @Column(name = "away_team", length = 16)
  private String awayTeam;

  @Column(name = "current_batter", length = 32)
  private String currentBatter;

  @Column(name = "current_pitcher", length = 32)
  private String currentPitcher;

  @Column(name = "bases", length = 20)
  private String bases; // JSON string: "[true,false,false]"

  @Column(name = "outs")
  private Integer outs;

  @Column(name = "balls")
  private Integer balls;

  @Column(name = "strikes")
  private Integer strikes;

  @Column(name = "timestamp")
  private Long timestamp;
}
