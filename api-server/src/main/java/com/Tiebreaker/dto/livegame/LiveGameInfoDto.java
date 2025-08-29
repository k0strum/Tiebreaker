package com.Tiebreaker.dto.livegame;

import lombok.Data;

@Data
public class LiveGameInfoDto {
  private String gameId;
  private String status; // READY, LIVE, FINISHED
  private Integer inning;
  private String half; // T (초), B (말)
  private Integer homeScore;
  private Integer awayScore;
  private String homeTeam;
  private String awayTeam;
  private String currentBatter;
  private String currentPitcher;
  private Boolean[] bases; // [1루, 2루, 3루]
  private Integer outs;
  private Integer balls;
  private Integer strikes;
  private Long timestamp;
}
