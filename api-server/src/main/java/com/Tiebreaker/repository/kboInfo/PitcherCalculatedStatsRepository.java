package com.Tiebreaker.repository.kboInfo;

import com.Tiebreaker.entity.kboInfo.PitcherCalculatedStats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PitcherCalculatedStatsRepository extends JpaRepository<PitcherCalculatedStats, Long> {

  Optional<PitcherCalculatedStats> findByPlayerId(Long playerId);

  // 방어율 순위
  @Query("SELECT pcs FROM PitcherCalculatedStats pcs " +
      "JOIN pcs.player p " +
      "WHERE pcs.earnedRunAverage > 0 " +
      "ORDER BY pcs.earnedRunAverage ASC")
  List<PitcherCalculatedStats> findEraRanking();

  // WHIP 순위
  @Query("SELECT pcs FROM PitcherCalculatedStats pcs " +
      "JOIN pcs.player p " +
      "WHERE pcs.whip > 0 " +
      "ORDER BY pcs.whip ASC")
  List<PitcherCalculatedStats> findWhipRanking();

}
