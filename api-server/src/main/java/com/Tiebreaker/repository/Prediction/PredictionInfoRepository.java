package com.Tiebreaker.repository.Prediction;

import com.Tiebreaker.entity.prediction.PredictionInfo;
import com.Tiebreaker.entity.auth.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface PredictionInfoRepository extends JpaRepository<PredictionInfo, Long> {

  /**
   * 특정 사용자의 특정 날짜 예측 결과 조회
   */
  Optional<PredictionInfo> findByMemberAndPredictionDate(Member member, LocalDate predictionDate);

  /**
   * 특정 사용자의 예측 결과 이력 조회 (최신순)
   */
  List<PredictionInfo> findByMemberOrderByPredictionDateDesc(Member member);

  /**
   * 특정 날짜의 모든 예측 결과 조회
   */
  List<PredictionInfo> findByPredictionDate(LocalDate predictionDate);

  /**
   * 특정 기간의 예측 결과 조회
   */
  List<PredictionInfo> findByPredictionDateBetween(LocalDate startDate, LocalDate endDate);

  /**
   * 특정 사용자의 특정 기간 예측 결과 조회
   */
  List<PredictionInfo> findByMemberAndPredictionDateBetween(Member member, LocalDate startDate, LocalDate endDate);

  /**
   * 특정 사용자의 총 예측 통계 조회
   */
  @Query("SELECT " +
      "COUNT(pi) as totalPredictions, " +
      "SUM(pi.correctPredictions) as totalCorrect, " +
      "SUM(pi.totalGames) as totalGames, " +
      "SUM(pi.totalMileage) as totalMileage " +
      "FROM PredictionInfo pi WHERE pi.member = :member")
  Object[] getMemberStatistics(@Param("member") Member member);

  /**
   * 특정 기간의 랭킹 조회 (정답 수 기준)
   */
  @Query("SELECT pi FROM PredictionInfo pi " +
      "WHERE pi.predictionDate BETWEEN :startDate AND :endDate " +
      "ORDER BY pi.correctPredictions DESC, pi.totalMileage DESC")
  List<PredictionInfo> findRankingByPeriod(@Param("startDate") LocalDate startDate,
      @Param("endDate") LocalDate endDate);

  /**
   * 특정 기간의 마일리지 랭킹 조회
   */
  @Query("SELECT pi FROM PredictionInfo pi " +
      "WHERE pi.predictionDate BETWEEN :startDate AND :endDate " +
      "ORDER BY pi.totalMileage DESC, pi.correctPredictions DESC")
  List<PredictionInfo> findMileageRankingByPeriod(@Param("startDate") LocalDate startDate,
      @Param("endDate") LocalDate endDate);

  /**
   * 특정 날짜의 예측 참여자 수 조회
   */
  @Query("SELECT COUNT(pi) FROM PredictionInfo pi WHERE pi.predictionDate = :date")
  Long countByPredictionDate(@Param("date") LocalDate date);
}
