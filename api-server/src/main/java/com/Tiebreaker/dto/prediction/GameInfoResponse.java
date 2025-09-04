package com.Tiebreaker.dto.prediction;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 예측 가능한 경기 정보 응답 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GameInfoResponse {

  private String gameId;
  private String homeTeam;
  private String awayTeam;
  private String status;
  private LocalDateTime gameDateTime;
  private boolean isPredictable; // 예측 가능 여부
  private String predictionDeadline; // 예측 마감 시간
}
