package com.Tiebreaker.repository.Prediction;

import com.Tiebreaker.entity.prediction.Prediction;
import com.Tiebreaker.entity.auth.Member;
import com.Tiebreaker.entity.livegame.LiveGameInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface PredictionRepository extends JpaRepository<Prediction, Long> {

  /**
   * 특정 사용자의 특정 날짜 예측 목록 조회
   */
  List<Prediction> findByMemberAndPredictionDate(Member member, LocalDate predictionDate);

  /**
   * 특정 경기에 대한 예측 조회
   */
  Optional<Prediction> findByMemberAndGameAndPredictionDate(Member member, LiveGameInfo game, LocalDate predictionDate);

  /**
   * 특정 날짜의 모든 예측 조회
   */
  List<Prediction> findByPredictionDate(LocalDate predictionDate);

  /**
   * 특정 경기에 대한 모든 예측 조회
   */
  List<Prediction> findByGameAndPredictionDate(LiveGameInfo game, LocalDate predictionDate);

  /**
   * 특정 사용자의 예측 이력 조회 (최신순)
   */
  List<Prediction> findByMemberOrderByPredictionDateDesc(Member member);

  /**
   * 특정 날짜의 예측 참여자 수 조회
   */
  @Query("SELECT COUNT(DISTINCT p.member) FROM Prediction p WHERE p.predictionDate = :date")
  Long countDistinctMembersByPredictionDate(@Param("date") LocalDate date);

  /**
   * 특정 경기의 예측 참여자 수 조회
   */
  @Query("SELECT COUNT(p) FROM Prediction p WHERE p.game = :game AND p.predictionDate = :date")
  Long countByGameAndPredictionDate(@Param("game") LiveGameInfo game, @Param("date") LocalDate date);
}
