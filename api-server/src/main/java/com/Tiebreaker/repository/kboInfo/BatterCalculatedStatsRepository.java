package com.Tiebreaker.repository.kboInfo;

import com.Tiebreaker.entity.kboInfo.BatterCalculatedStats;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BatterCalculatedStatsRepository extends JpaRepository<BatterCalculatedStats, Long> {

  Optional<BatterCalculatedStats> findByPlayerId(Long playerId);

}
