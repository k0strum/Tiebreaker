package com.Tiebreaker.repository.kboInfo;

import com.Tiebreaker.entity.kboInfo.BatterStats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BatterStatsRepository extends JpaRepository<BatterStats, Long> {

  Optional<BatterStats> findByPlayerId(Long playerId);

  // 홈런 순위
  @Query("SELECT bs FROM BatterStats bs " +
      "JOIN bs.player p " +
      "WHERE bs.homeRuns > 0 " +
      "ORDER BY bs.homeRuns DESC")
  List<BatterStats> findHomeRunRanking();

  // 타점 순위
  @Query("SELECT bs FROM BatterStats bs " +
      "JOIN bs.player p " +
      "WHERE bs.runsBattedIn > 0 " +
      "ORDER BY bs.runsBattedIn DESC")
  List<BatterStats> findRbiRanking();

  // 도루 순위
  @Query("SELECT bs FROM BatterStats bs " +
      "JOIN bs.player p " +
      "WHERE bs.stolenBases > 0 " +
      "ORDER BY bs.stolenBases DESC")
  List<BatterStats> findStolenBasesRanking();
}
