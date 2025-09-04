package com.Tiebreaker.repository.Prediction;

import com.Tiebreaker.entity.prediction.PredictionRanking;
import com.Tiebreaker.entity.auth.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface PredictionRankingRepository extends JpaRepository<PredictionRanking, Long> {

  /**
   * 특정 사용자의 특정 기간 랭킹 조회
   */
  Optional<PredictionRanking> findByMemberAndRankingTypeAndPeriodStartAndPeriodEnd(
      Member member, String rankingType, LocalDate periodStart, LocalDate periodEnd);

  /**
   * 특정 랭킹 타입의 랭킹 조회 (정답 수 기준)
   */
  @Query("SELECT pr FROM PredictionRanking pr " +
      "WHERE pr.rankingType = :rankingType " +
      "AND pr.periodStart = :periodStart " +
      "AND pr.periodEnd = :periodEnd " +
      "ORDER BY pr.totalCorrect DESC, pr.accuracy DESC, pr.totalMileage DESC")
  List<PredictionRanking> findRankingByTypeAndPeriod(
      @Param("rankingType") String rankingType,
      @Param("periodStart") LocalDate periodStart,
      @Param("periodEnd") LocalDate periodEnd);

  /**
   * 특정 랭킹 타입의 마일리지 랭킹 조회
   */
  @Query("SELECT pr FROM PredictionRanking pr " +
      "WHERE pr.rankingType = :rankingType " +
      "AND pr.periodStart = :periodStart " +
      "AND pr.periodEnd = :periodEnd " +
      "ORDER BY pr.totalMileage DESC, pr.totalCorrect DESC, pr.accuracy DESC")
  List<PredictionRanking> findMileageRankingByTypeAndPeriod(
      @Param("rankingType") String rankingType,
      @Param("periodStart") LocalDate periodStart,
      @Param("periodEnd") LocalDate periodEnd);

  /**
   * 특정 사용자의 랭킹 이력 조회
   */
  List<PredictionRanking> findByMemberAndRankingTypeOrderByPeriodStartDesc(
      Member member, String rankingType);

  /**
   * 특정 랭킹 타입의 최신 랭킹 조회
   */
  @Query("SELECT pr FROM PredictionRanking pr " +
      "WHERE pr.rankingType = :rankingType " +
      "ORDER BY pr.periodStart DESC, pr.totalCorrect DESC")
  List<PredictionRanking> findLatestRankingByType(@Param("rankingType") String rankingType);

  /**
   * 특정 사용자의 랭킹 순위 조회
   */
  @Query("SELECT COUNT(pr) + 1 FROM PredictionRanking pr " +
      "WHERE pr.rankingType = :rankingType " +
      "AND pr.periodStart = :periodStart " +
      "AND pr.periodEnd = :periodEnd " +
      "AND (pr.totalCorrect > :totalCorrect OR " +
      "(pr.totalCorrect = :totalCorrect AND pr.accuracy > :accuracy) OR " +
      "(pr.totalCorrect = :totalCorrect AND pr.accuracy = :accuracy AND pr.totalMileage > :totalMileage))")
  Long findRankingPosition(
      @Param("rankingType") String rankingType,
      @Param("periodStart") LocalDate periodStart,
      @Param("periodEnd") LocalDate periodEnd,
      @Param("totalCorrect") Integer totalCorrect,
      @Param("accuracy") Double accuracy,
      @Param("totalMileage") Integer totalMileage);

  /**
   * 특정 기간의 랭킹 데이터 존재 여부 확인
   */
  boolean existsByRankingTypeAndPeriodStartAndPeriodEnd(
      String rankingType, LocalDate periodStart, LocalDate periodEnd);

  /**
   * 특정 랭킹 타입의 모든 기간 조회
   */
  @Query("SELECT DISTINCT pr.periodStart, pr.periodEnd FROM PredictionRanking pr " +
      "WHERE pr.rankingType = :rankingType " +
      "ORDER BY pr.periodStart DESC")
  List<Object[]> findDistinctPeriodsByRankingType(@Param("rankingType") String rankingType);
}
