package com.Tiebreaker.repository.kboInfo;

import com.Tiebreaker.entity.kboInfo.PitcherStats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PitcherStatsRepository extends JpaRepository<PitcherStats, Long> {

  Optional<PitcherStats> findByPlayerId(Long playerId);

  // 승수 순위
  @Query("SELECT ps FROM PitcherStats ps " +
      "JOIN ps.player p " +
      "WHERE ps.wins > 0 " +
      "ORDER BY ps.wins DESC")
  List<PitcherStats> findWinsRanking();

  // 세이브 순위
  @Query("SELECT ps FROM PitcherStats ps " +
      "JOIN ps.player p " +
      "WHERE ps.saves > 0 " +
      "ORDER BY ps.saves DESC")
  List<PitcherStats> findSavesRanking();

  // 홀드 순위
  @Query("SELECT ps FROM PitcherStats ps " +
      "JOIN ps.player p " +
      "WHERE ps.holds > 0 " +
      "ORDER BY ps.holds DESC")
  List<PitcherStats> findHoldsRanking();

  // 탈삼진 순위
  @Query("SELECT ps FROM PitcherStats ps " +
      "JOIN ps.player p " +
      "WHERE ps.strikeouts > 0 " +
      "ORDER BY ps.strikeouts DESC")
  List<PitcherStats> findStrikeoutsRanking();

}
