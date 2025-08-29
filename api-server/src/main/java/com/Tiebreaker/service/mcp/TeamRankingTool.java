package com.Tiebreaker.service.mcp;

import com.Tiebreaker.dto.kboInfo.CurrentTeamRankResponseDto;
import com.Tiebreaker.service.kboinfo.KboTeamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * 팀 순위 조회 MCP 도구
 * 사용 예: "팀 순위 알려줘"
 */
@Component
public class TeamRankingTool implements McpTool {

  @Autowired
  private KboTeamService teamService;

  @Override
  public Object execute(Map<String, Object> arguments) {
    try {
      List<CurrentTeamRankResponseDto> ranks = teamService.getTeamRank();
      return Map.of(
          "count", ranks != null ? ranks.size() : 0,
          "ranks", ranks);
    } catch (Exception e) {
      return Map.of("error", "팀 순위 조회 중 오류: " + e.getMessage());
    }
  }

  @Override
  public String getDescription() {
    return "현재 시즌의 팀 순위를 반환합니다.";
  }

  @Override
  public String getName() {
    return "getTeamRanking";
  }
}
