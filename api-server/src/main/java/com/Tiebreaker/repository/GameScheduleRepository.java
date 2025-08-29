package com.Tiebreaker.repository;

import com.Tiebreaker.entity.livegame.GameSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface GameScheduleRepository extends JpaRepository<GameSchedule, String> {
  List<GameSchedule> findByGameDate(String gameDate);

  // 스케줄러용 메서드들
  List<GameSchedule> findByGameDateTimeBetweenAndStatusCode(String start, String end, String statusCode);

  List<GameSchedule> findByGameDateTimeBeforeAndStatusCode(String before, String statusCode);

  // 디버깅용: 모든 BEFORE 상태 경기 조회
  List<GameSchedule> findByStatusCode(String statusCode);

  // 최적화된 쿼리: 특정 시간 범위 내의 BEFORE 상태 경기들
  @Query("SELECT g FROM GameSchedule g WHERE g.statusCode = 'BEFORE' AND " +
      "REPLACE(g.gameDateTime, ' ', 'T') BETWEEN :startTime AND :endTime " +
      "ORDER BY g.gameDateTime ASC")
  List<GameSchedule> findUpcomingGamesInRange(@Param("startTime") String startTime,
      @Param("endTime") String endTime);
}
