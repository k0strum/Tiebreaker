package com.Tiebreaker.dto.prediction;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * 랭킹 정보 응답 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RankingResponse {

  private Long memberId;
  private String nickname;
  private String profileImage;
  private Integer totalCorrect; // 총 정답 수
  private Integer totalGames; // 총 경기 수
  private Double accuracy; // 적중률
  private Integer totalMileage; // 총 획득 마일리지
  private Integer rankingPosition; // 랭킹 순위
  private String rankingType; // 랭킹 타입 (DAILY, MONTHLY, YEARLY)
  private LocalDate periodStart; // 기간 시작일
  private LocalDate periodEnd; // 기간 종료일
}
