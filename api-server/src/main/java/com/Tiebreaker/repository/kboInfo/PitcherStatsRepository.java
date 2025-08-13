package com.Tiebreaker.repository.kboInfo;

import com.Tiebreaker.entity.kboInfo.PitcherStats;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PitcherStatsRepository extends JpaRepository<PitcherStats, Long> {

  Optional<PitcherStats> findByPlayerId(Long playerId);

}
