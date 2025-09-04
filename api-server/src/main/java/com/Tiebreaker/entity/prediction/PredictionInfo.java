package com.Tiebreaker.entity.prediction;

import com.Tiebreaker.entity.BaseTimeEntity;
import com.Tiebreaker.entity.auth.Member;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/**
 * 일일 예측 결과 요약 정보
 * - 사용자가 하루에 예측한 모든 경기의 결과를 요약
 * - 마일리지 지급 및 통계 계산에 사용
 */
@Entity
@Table(name = "prediction_info")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PredictionInfo extends BaseTimeEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "member_id", nullable = false)
  private Member member;

  @Column(name = "prediction_date", nullable = false)
  private LocalDate predictionDate; // 예측 날짜

  @Column(name = "total_games", nullable = false)
  private Integer totalGames; // 총 경기 수

  @Column(name = "correct_predictions", nullable = false)
  private Integer correctPredictions; // 정답 수

  @Column(name = "earned_mileage", nullable = false)
  private Integer earnedMileage; // 획득 마일리지 (기본 가중치)

  @Column(name = "bonus_mileage", nullable = false)
  private Integer bonusMileage = 0; // 보너스 마일리지 (취소/연기 등)

  @Column(name = "total_mileage", nullable = false)
  private Integer totalMileage; // 총 획득 마일리지 (earned + bonus)

  // 하루에 한 번만 결과 생성되도록 제약
  @Table(uniqueConstraints = {
      @UniqueConstraint(columnNames = { "member_id", "prediction_date" })
  })
  public static class PredictionInfoConstraints {
  }
}
