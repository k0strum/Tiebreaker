package com.Tiebreaker.repository.kboInfo;

import com.Tiebreaker.entity.kboInfo.PitcherMonthlyStats;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PitcherMonthlyStatsRepository extends JpaRepository<PitcherMonthlyStats, Long> {

  Optional<PitcherMonthlyStats> findByPlayer_IdAndYearAndMonth(Long playerId, Integer year, Integer month);

  List<PitcherMonthlyStats> findByPlayer_IdAndYearOrderByMonthAsc(Long playerId, Integer year);
}
