package com.Tiebreaker.dto.prediction;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 예측 통계 응답 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PredictionStatsResponse {

  private Long totalPredictions; // 총 예측 횟수
  private Long totalCorrect; // 총 정답 수
  private Long totalGames; // 총 경기 수
  private Long totalMileage; // 총 획득 마일리지
  private Double accuracy; // 적중률
}
