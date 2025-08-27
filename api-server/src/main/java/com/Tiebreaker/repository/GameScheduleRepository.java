package com.Tiebreaker.repository;

import com.Tiebreaker.entity.GameSchedule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GameScheduleRepository extends JpaRepository<GameSchedule, String> {
  List<GameSchedule> findByGameDate(String gameDate);
}
