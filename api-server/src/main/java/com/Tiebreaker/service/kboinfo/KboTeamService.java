package com.Tiebreaker.service.kboinfo;

import com.Tiebreaker.constant.TeamLogoConstants;
import com.Tiebreaker.dto.kboInfo.CurrentTeamRankResponseDto;
import com.Tiebreaker.entity.kboInfo.TeamRank;
import com.Tiebreaker.repository.kboInfo.TeamRankRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class KboTeamService {

  private final TeamRankRepository teamRankRepository;

  @Transactional(readOnly = true)
  public List<CurrentTeamRankResponseDto> getTeamRank() {
    List<TeamRank> teamRanks = teamRankRepository.findAllByOrderByRankAsc();
    
    // 각 팀에 로고 URL 설정
    teamRanks.forEach(team -> {
      String logoUrl = TeamLogoConstants.getTeamLogo(team.getTeamName());
      team.setTeamLogoUrl(logoUrl);
    });
    
    return teamRanks.stream()
      .map(team -> new CurrentTeamRankResponseDto(
        team.getRegDate(),
        team.getUpdateDate(),
        team.getTeamLogoUrl(),
        team.getRank(),
        team.getTeamName(),
        team.getPlays(),
        team.getWins(),
        team.getLosses(),
        team.getDraws(),
        team.getWinRate(),
        team.getGameBehind(),
        team.getStreak()
      ))
      .collect(Collectors.toList());
  }
}
