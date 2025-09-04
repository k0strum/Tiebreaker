package com.Tiebreaker.dto.prediction;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 예측 정보 응답 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PredictionResponse {

  private Long id;
  private String gameId;
  private String homeTeam;
  private String awayTeam;
  private String predictedWinner;
  private LocalDate predictionDate;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
}
