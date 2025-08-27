package com.Tiebreaker.dto.kboInfo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class GameScheduleItemDto {
  private String gameId;
  private String gameDate; // yyyy-MM-dd
  private String gameDateTime; // ISO-8601
  private String stadium;

  private String homeTeamCode;
  private String homeTeamName;
  private Integer homeTeamScore;

  private String awayTeamCode;
  private String awayTeamName;
  private Integer awayTeamScore;

  private String statusCode; // BEFORE, LIVE, RESULT, READY
  private String winner; // HOME, AWAY, DRAW

  private Boolean suspended; // 경기 중단 여부
  private String broadChannel; // 중계 채널

  private String homeStarterName;
  private String awayStarterName;

  private String roundCode; // kbo_r 등
}
