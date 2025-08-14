package com.Tiebreaker.repository.kboInfo;

import com.Tiebreaker.entity.kboInfo.BatterCalculatedStats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BatterCalculatedStatsRepository extends JpaRepository<BatterCalculatedStats, Long> {

  Optional<BatterCalculatedStats> findByPlayerId(Long playerId);

  // 타율 순위
  @Query("SELECT bcs FROM BatterCalculatedStats bcs " +
      "JOIN bcs.player p " +
      "WHERE bcs.battingAverage > 0 " +
      "ORDER BY bcs.battingAverage DESC")
  List<BatterCalculatedStats> findBattingAverageRanking();

  // 출루율 순위
  @Query("SELECT bcs FROM BatterCalculatedStats bcs " +
      "JOIN bcs.player p " +
      "WHERE bcs.onBasePercentage > 0 " +
      "ORDER BY bcs.onBasePercentage DESC")
  List<BatterCalculatedStats> findOnBasePercentageRanking();

  // OPS 순위
  @Query("SELECT bcs FROM BatterCalculatedStats bcs " +
      "JOIN bcs.player p " +
      "WHERE bcs.ops > 0 " +
      "ORDER BY bcs.ops DESC")
  List<BatterCalculatedStats> findOpsRanking();

}
