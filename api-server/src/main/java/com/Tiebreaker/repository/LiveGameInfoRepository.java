package com.Tiebreaker.repository;

import com.Tiebreaker.entity.livegame.LiveGameInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

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
}
