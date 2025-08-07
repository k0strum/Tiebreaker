package com.Tiebreaker.service.kboInfo;

import com.Tiebreaker.dto.kboInfo.KboRankDto;
import com.Tiebreaker.dto.kboInfo.TeamRankDto;
import com.Tiebreaker.repository.TeamRankRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.Tiebreaker.entity.kboInfo.TeamRank;

@Service
@RequiredArgsConstructor
public class RankService {

  private final TeamRankRepository teamRankRepository;

  @Transactional
  public void updateRanks(KboRankDto kboRankDto) {
    // DTO 리스트 하나씩 순회
    kboRankDto.getData().forEach(teamDto -> {
      // DB에서 해당 팀 이름으로 기존 데이터를 찾아옴
      TeamRank teamRank = teamRankRepository.findByName(teamDto.getName());

      if (teamRank == null) {
        // 기존 데이터가 없으면 새로운 Entity 생성
        teamRank = new TeamRank();
        teamRank.setName(teamDto.getName());
      }

      // 순위정보 업데이트
      teamRank.setTeamRank(teamDto.getRank());

      // DB에 저장
      teamRankRepository.save(teamRank);
    });
    System.out.println("Team rank data has been successfully updated in the database.");
  }

}
