package com.Tiebreaker.repository;

import com.Tiebreaker.entity.kboInfo.BatterStats;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BatterStatsRepository extends JpaRepository<BatterStats, Long> {
  Optional<BatterStats> findByPlayerId(Long playerId);
}
