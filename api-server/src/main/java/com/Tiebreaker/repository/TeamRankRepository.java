package com.Tiebreaker.repository;

import com.Tiebreaker.entity.kboInfo.TeamRank;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeamRankRepository extends JpaRepository<TeamRank, Long> {
  // 팀 이름으로 순위 정보를 찾기 위한 메서드
  TeamRank findByName(String name);
}
