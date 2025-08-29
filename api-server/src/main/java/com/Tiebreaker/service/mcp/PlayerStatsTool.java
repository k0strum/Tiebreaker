package com.Tiebreaker.service.mcp;

import com.Tiebreaker.dto.kboInfo.PlayerDetailResponseDto;
import com.Tiebreaker.entity.kboInfo.Player;
import com.Tiebreaker.service.kboinfo.KboPlayerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 선수 성적 조회 MCP 도구
 * 입력은 기본적으로 선수 이름만 받습니다. (동명이인인 경우 후보를 반환)
 */
@Component
public class PlayerStatsTool implements McpTool {

  @Autowired
  private KboPlayerService playerService;

  @Override
  public Object execute(Map<String, Object> arguments) {
    String playerName = arguments == null ? null : (String) arguments.get("playerName");
    Long playerId = extractPlayerId(arguments); // 후보 클릭 재조회 용도

    // 1) playerId로 직접 재조회가 오면 상세만 반환
    if (playerId != null) {
      return buildDetailResponseByPlayerId(playerId);
    }

    // 2) 이름 기반 검색 (단일 입력 원칙)
    if (playerName == null || playerName.trim().isEmpty()) {
      return Map.of(
          "error", "선수 이름이 필요합니다.",
          "example", Map.of("toolName", "getPlayerStats", "arguments", Map.of("playerName", "이승현")));
    }

    String normalized = normalizeName(playerName);
    List<Player> candidates = playerService.searchPlayersByName(normalized);
    if (candidates == null || candidates.isEmpty()) {
      return Map.of("error", "선수를 찾을 수 없습니다: " + playerName);
    }

    // 정확 이름 매칭만 추출
    List<Player> exactMatches = candidates.stream()
        .filter(p -> p.getPlayerName() != null && p.getPlayerName().equalsIgnoreCase(normalized))
        .collect(Collectors.toList());

    Player chosen = null;
    if (exactMatches.size() == 1) {
      chosen = exactMatches.get(0);
    } else if (exactMatches.size() > 1) {
      // 동일 이름으로 다수 존재 → 후보 리스트 반환
      return buildCandidateList(exactMatches, "동명이인이 여러 명입니다. 후보 중 하나를 선택해주세요.");
    } else if (candidates.size() == 1) {
      chosen = candidates.get(0);
    } else {
      // 이름 포함 검색 다수 → 후보 리스트 반환
      return buildCandidateList(candidates, "여러 명이 검색되었습니다. 후보 중 하나를 선택해주세요.");
    }

    // 선택된 선수 상세 조회
    PlayerDetailResponseDto detail = playerService.getPlayerDetail(chosen.getId());

    Map<String, Object> stats;
    if (detail.getBatterStats() != null) {
      var b = detail.getBatterStats();
      stats = Map.of(
          "type", "Batter",
          "battingAverage", b.getBattingAverage(),
          "onBasePercentage", b.getOnBasePercentage(),
          "sluggingPercentage", b.getSluggingPercentage(),
          "ops", b.getOps(),
          "homeRuns", b.getHomeRuns(),
          "runsBattedIn", b.getRunsBattedIn(),
          "hits", b.getHits(),
          "games", b.getGames());
    } else if (detail.getPitcherStats() != null) {
      var p = detail.getPitcherStats();
      stats = Map.of(
          "type", "Pitcher",
          "era", p.getEarnedRunAverage(),
          "wins", p.getWins(),
          "losses", p.getLosses(),
          "saves", p.getSaves(),
          "whip", p.getWhip(),
          "strikeouts", p.getStrikeouts(),
          "games", p.getGames());
    } else {
      return Map.of(
          "playerId", chosen.getId(),
          "playerName", chosen.getPlayerName(),
          "teamName", chosen.getTeamName(),
          "message", "해당 선수의 시즌 스탯이 없어 기본 정보만 반환합니다.");
    }

    String message = chosen.getPlayerName() + " 선수의 최신 시즌 요약입니다.";

    return Map.of(
        "playerId", chosen.getId(),
        "playerName", chosen.getPlayerName(),
        "teamName", chosen.getTeamName(),
        "stats", stats,
        "message", message);
  }

  private Object buildCandidateList(List<Player> list, String message) {
    List<?> candidateSummaries = list.stream()
        .map(p -> Map.of(
            "playerId", p.getId(),
            "playerName", p.getPlayerName(),
            "teamName", p.getTeamName(),
            "position", p.getPosition()))
        .distinct()
        .limit(10)
        .collect(Collectors.toList());
    return Map.of(
        "candidates", candidateSummaries,
        "message", message);
  }

  private String normalizeName(String name) {
    if (name == null)
      return null;
    return name.trim();
  }

  private Object buildDetailResponseByPlayerId(Long playerId) {
    try {
      PlayerDetailResponseDto detail = playerService.getPlayerDetail(playerId);
      List<Player> byId = playerService.getAllPlayers().stream()
          .filter(p -> Objects.equals(p.getId(), playerId))
          .collect(Collectors.toList());
      Player base = byId.isEmpty() ? null : byId.get(0);

      if (detail.getBatterStats() != null) {
        var b = detail.getBatterStats();
        return Map.of(
            "playerId", playerId,
            "playerName", base != null ? base.getPlayerName() : null,
            "teamName", base != null ? base.getTeamName() : null,
            "stats", Map.of(
                "type", "Batter",
                "battingAverage", b.getBattingAverage(),
                "onBasePercentage", b.getOnBasePercentage(),
                "sluggingPercentage", b.getSluggingPercentage(),
                "ops", b.getOps(),
                "homeRuns", b.getHomeRuns(),
                "runsBattedIn", b.getRunsBattedIn(),
                "hits", b.getHits(),
                "games", b.getGames()),
            "message", "선택된 선수의 최신 시즌 요약입니다.");
      }
      if (detail.getPitcherStats() != null) {
        var p = detail.getPitcherStats();
        return Map.of(
            "playerId", playerId,
            "playerName", base != null ? base.getPlayerName() : null,
            "teamName", base != null ? base.getTeamName() : null,
            "stats", Map.of(
                "type", "Pitcher",
                "era", p.getEarnedRunAverage(),
                "wins", p.getWins(),
                "losses", p.getLosses(),
                "saves", p.getSaves(),
                "whip", p.getWhip(),
                "strikeouts", p.getStrikeouts(),
                "games", p.getGames()),
            "message", "선택된 선수의 최신 시즌 요약입니다.");
      }
      return Map.of(
          "playerId", playerId,
          "playerName", base != null ? base.getPlayerName() : null,
          "teamName", base != null ? base.getTeamName() : null,
          "message", "해당 선수의 시즌 스탯이 없어 기본 정보만 반환합니다.");
    } catch (Exception e) {
      return Map.of("error", "선수 상세 조회 중 오류: " + e.getMessage());
    }
  }

  private Long extractPlayerId(Map<String, Object> arguments) {
    if (arguments == null)
      return null;
    Object raw = arguments.get("playerId");
    if (raw == null)
      return null;
    if (raw instanceof Number n)
      return n.longValue();
    try {
      return Long.parseLong(String.valueOf(raw));
    } catch (Exception e) {
      return null;
    }
  }

  @Override
  public String getDescription() {
    return "선수 이름으로 조회하고, 동명이인이 여러 명이면 후보를 반환합니다. 후보 선택 시 playerId로 재조회하세요.";
  }

  @Override
  public String getName() {
    return "getPlayerStats";
  }
}
