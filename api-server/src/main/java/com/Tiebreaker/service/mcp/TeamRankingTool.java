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
      
      if (ranks == null) {
        return Map.of(
            "error", "팀 순위 데이터를 가져올 수 없습니다.",
            "detail", "서비스에서 null 응답을 받았습니다.");
      }
      
      return Map.of(
          "count", ranks.size(),
          "ranks", ranks,
          "message", "현재 시즌 팀 순위입니다.");
    } catch (Exception e) {
      return Map.of(
          "error", "팀 순위 조회 중 오류가 발생했습니다.",
          "detail", e.getMessage());
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
