package com.Tiebreaker.repository.kboInfo;

import com.Tiebreaker.entity.kboInfo.TeamRank;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TeamRankRepository extends JpaRepository<TeamRank, Long> {
  // 팀 이름으로 순위 정보를 찾기 위한 메서드
  TeamRank findByTeamName(String teamName);

  // 순위 기준 오름차순 정렬
  List<TeamRank> findAllByOrderByRankAsc();
}
