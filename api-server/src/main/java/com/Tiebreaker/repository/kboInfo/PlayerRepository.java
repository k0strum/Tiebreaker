package com.Tiebreaker.repository.kboInfo;

import com.Tiebreaker.constant.PlayerStatus;
import com.Tiebreaker.constant.PlayerType;
import com.Tiebreaker.entity.kboInfo.Player;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlayerRepository extends JpaRepository<Player, Long> {

  // 선수 이름으로 검색
  Optional<Player> findByPlayerName(String playerName);

  // 2. 특정 상태(예: ACTIVE)의 모든 선수를 조회하는 기능 (가장 중요!)
  // 이 메서드는 매일 기록을 업데이트할 대상을 찾기 위해 Python 크롤러가 호출할 API에서 사용됩니다.
  List<Player> findByStatus(PlayerStatus status);

  // 팀별 선수 조회
  List<Player> findByTeamName(String teamName);

  // 선수 타입별 조회
  List<Player> findByPlayerType(PlayerType playerType);

  // 선수 이름으로 부분 검색 (대소문자 구분 없음)
  List<Player> findByPlayerNameContainingIgnoreCase(String playerName);

}
