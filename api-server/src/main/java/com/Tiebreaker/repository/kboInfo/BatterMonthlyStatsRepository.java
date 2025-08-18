package com.Tiebreaker.repository.kboInfo;

import com.Tiebreaker.entity.kboInfo.BatterMonthlyStats;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BatterMonthlyStatsRepository extends JpaRepository<BatterMonthlyStats, Long> {

  Optional<BatterMonthlyStats> findByPlayer_IdAndYearAndMonth(Long playerId, Integer year, Integer month);

  List<BatterMonthlyStats> findByPlayer_IdAndYearOrderByMonthAsc(Long playerId, Integer year);
}
