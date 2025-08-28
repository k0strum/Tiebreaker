package com.Tiebreaker.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "game_schedule", indexes = {
    @Index(name = "idx_game_schedule_date", columnList = "game_date"),
    @Index(name = "idx_game_schedule_status", columnList = "status_code")
})
public class GameSchedule {

  @Id
  @Column(name = "game_id", length = 32)
  private String gameId;

  @Column(name = "game_date", length = 10, nullable = false)
  private String gameDate; // yyyy-MM-dd

  @Column(name = "game_datetime", length = 32)
  private String gameDateTime; // ISO-8601

  @Column(name = "stadium", length = 32)
  private String stadium;

  @Column(name = "home_team_code", length = 8)
  private String homeTeamCode;

  @Column(name = "home_team_name", length = 32)
  private String homeTeamName;

  @Column(name = "home_team_score")
  private Integer homeTeamScore;

  @Column(name = "away_team_code", length = 8)
  private String awayTeamCode;

  @Column(name = "away_team_name", length = 32)
  private String awayTeamName;

  @Column(name = "away_team_score")
  private Integer awayTeamScore;

  @Column(name = "status_code", length = 16)
  private String statusCode; // BEFORE, LIVE, RESULT, READY

  @Column(name = "status_info", length = 32)
  private String statusInfo; // 경기 진행 상황 (예: "경기전", "9회말", "경기취소")

  @Column(name = "winner", length = 8)
  private String winner; // HOME, AWAY, DRAW

  @Column(name = "suspended")
  private Boolean suspended;

  @Column(name = "broad_channel", length = 64)
  private String broadChannel;

  @Column(name = "home_starter_name", length = 32)
  private String homeStarterName;

  @Column(name = "away_starter_name", length = 32)
  private String awayStarterName;

  @Column(name = "round_code", length = 16)
  private String roundCode;
}
