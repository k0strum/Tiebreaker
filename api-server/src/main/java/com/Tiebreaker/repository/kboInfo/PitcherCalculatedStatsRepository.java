package com.Tiebreaker.repository.kboInfo;

import com.Tiebreaker.entity.kboInfo.PitcherCalculatedStats;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PitcherCalculatedStatsRepository extends JpaRepository<PitcherCalculatedStats, Long> {

  Optional<PitcherCalculatedStats> findByPlayerId(Long playerId);

}
