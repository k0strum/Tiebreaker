package com.Tiebreaker.repository;

import com.Tiebreaker.entity.livegame.LiveGameInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface LiveGameInfoRepository extends JpaRepository<LiveGameInfo, String> {

  /**
   * 특정 경기의 최신 실시간 정보 조회 (timestamp 기준)
   */
  Optional<LiveGameInfo> findTop1ByGameIdOrderByTimestampDesc(String gameId);

  /**
   * 특정 상태의 경기들 조회
   */
  List<LiveGameInfo> findByStatus(String status);

  /**
   * 진행 중인 경기들 조회
   */
  List<LiveGameInfo> findByStatusIn(List<String> statuses);

  /**
   * 오늘 생성되거나 갱신된 활성 경기들만 조회
   * 서버 재시작 후 과거 데이터가 READY 상태로 남는 문제 해결
   */
  @Query("SELECT l FROM LiveGameInfo l WHERE l.status IN ('READY', 'LIVE') " +
      "AND DATE(l.updateDate) = :today " +
      "ORDER BY l.timestamp DESC")
  List<LiveGameInfo> findTodayActiveGames(@Param("today") LocalDate today);

  /**
   * 지정된 날짜 이전의 READY 상태 데이터 조회 (과거 데이터 정리용)
   */
  @Query("SELECT l FROM LiveGameInfo l WHERE l.status = 'READY' " +
      "AND DATE(l.updateDate) < :beforeDate " +
      "ORDER BY l.updateDate ASC")
  List<LiveGameInfo> findOldReadyGames(@Param("beforeDate") LocalDate beforeDate);
}
