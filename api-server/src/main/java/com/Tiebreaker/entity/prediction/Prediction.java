package com.Tiebreaker.entity.prediction;

import com.Tiebreaker.entity.BaseTimeEntity;
import com.Tiebreaker.entity.auth.Member;
import com.Tiebreaker.entity.livegame.LiveGameInfo;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/**
 * 개별 경기별 예측 정보
 * - 사용자가 특정 경기에 대해 어떤 팀이 이길지 예측
 * - 하루에 여러 경기가 있을 수 있으므로 경기별로 예측 저장
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Prediction extends BaseTimeEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "member_id", nullable = false)
  private Member member;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "game_id", nullable = false)
  private LiveGameInfo game;

  @Column(name = "predicted_winner", nullable = false)
  private String predictedWinner; // "HOME" or "AWAY"

  @Column(name = "prediction_date", nullable = false)
  private LocalDate predictionDate; // 예측 날짜

  // 하루에 한 경기에 대해 한 번만 예측 가능하도록 제약
  @Table(uniqueConstraints = {
      @UniqueConstraint(columnNames = { "member_id", "game_id", "prediction_date" })
  })
  public static class PredictionConstraints {
  }
}
