package com.Tiebreaker.repository;

import com.Tiebreaker.constant.PlayerStatus;
import com.Tiebreaker.entity.kboInfo.Player;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PlayerRepository extends JpaRepository<Player, Long> {

  // 선수 이름으로 검색
  Optional<Player> findByPlayerName(String playerName);

  // 2. 특정 상태(예: ACTIVE)의 모든 선수를 조회하는 기능 (가장 중요!)
  // 이 메서드는 매일 기록을 업데이트할 대상을 찾기 위해 Python 크롤러가 호출할 API에서 사용됩니다.
  List<Player> findByStatus(PlayerStatus status);

}
