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
 * 예측 랭킹 정보
 * - 일간/월간/연간 적중 횟수 랭킹을 위한 집계 데이터
 * - 성능 최적화를 위해 미리 계산된 값 저장
 */
@Entity
@Table(name = "prediction_ranking")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PredictionRanking extends BaseTimeEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "member_id", nullable = false)
  private Member member;

  @Column(name = "ranking_type", nullable = false)
  private String rankingType; // "DAILY", "MONTHLY", "YEARLY"

  @Column(name = "period_start", nullable = false)
  private LocalDate periodStart; // 기간 시작일

  @Column(name = "period_end", nullable = false)
  private LocalDate periodEnd; // 기간 종료일

  @Column(name = "total_correct", nullable = false)
  private Integer totalCorrect = 0; // 총 정답 수

  @Column(name = "total_games", nullable = false)
  private Integer totalGames = 0; // 총 경기 수

  @Column(name = "accuracy", nullable = false)
  private Double accuracy = 0.0; // 적중률 (totalCorrect / totalGames)

  @Column(name = "total_mileage", nullable = false)
  private Integer totalMileage = 0; // 총 획득 마일리지

  @Column(name = "ranking_position")
  private Integer rankingPosition; // 랭킹 순위 (선택사항)

  // 중복 생성 방지를 위한 제약
  @Table(uniqueConstraints = {
      @UniqueConstraint(columnNames = { "member_id", "ranking_type", "period_start", "period_end" })
  })
  public static class PredictionRankingConstraints {
  }
}
