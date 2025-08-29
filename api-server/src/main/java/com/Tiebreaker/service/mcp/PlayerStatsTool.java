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
 * 사용 예: "김현수 선수 올해 성적 어때?"
 */
@Component
public class PlayerStatsTool implements McpTool {

  @Autowired
  private KboPlayerService playerService;

  @Override
  public Object execute(Map<String, Object> arguments) {
    String playerName = arguments == null ? null : (String) arguments.get("playerName");
    // 선택적 필터
    String teamName = arguments == null ? null : (String) arguments.get("teamName");
    String position = arguments == null ? null : (String) arguments.get("position");
    Long playerId = extractPlayerId(arguments);

    if ((playerName == null || playerName.trim().isEmpty()) && playerId == null) {
      return Map.of(
          "error", "선수 식별 정보가 필요합니다.",
          "example", Map.of(
              "byName",
              Map.of("toolName", "getPlayerStats", "arguments", Map.of("playerName", "이승현", "teamName", "키움")),
              "byId", Map.of("toolName", "getPlayerStats", "arguments", Map.of("playerId", 12345))));
    }

    // 1) playerId로 직접 조회가 오면 바로 상세 조회
    if (playerId != null) {
      return buildDetailResponseByPlayerId(playerId);
    }

    // 2) 이름으로 후보 검색
    List<Player> candidates = playerService.searchPlayersByName(playerName);
    if (candidates == null || candidates.isEmpty()) {
      return Map.of("error", "선수를 찾을 수 없습니다: " + playerName);
    }

    // 3) 선택적 필터 적용 (teamName, position)
    List<Player> filtered = candidates;
    if (teamName != null && !teamName.isBlank()) {
      filtered = filtered.stream()
          .filter(p -> p.getTeamName() != null && p.getTeamName().equalsIgnoreCase(teamName))
          .collect(Collectors.toList());
    }
    if (position != null && !position.isBlank()) {
      filtered = filtered.stream()
          .filter(p -> p.getPosition() != null && p.getPosition().toLowerCase().contains(position.toLowerCase()))
          .collect(Collectors.toList());
    }

    // 4) 정확 일치 우선(이름), 그 다음 필터 결과
    Player chosen = null;
    if (filtered != null && !filtered.isEmpty()) {
      chosen = filtered.stream()
          .filter(p -> p.getPlayerName() != null && p.getPlayerName().equalsIgnoreCase(playerName))
          .findFirst()
          .orElse(filtered.size() == 1 ? filtered.get(0) : null);
    }

    // 5) 여전히 모호하면 후보 리스트 반환
    if (chosen == null) {
      // 후보 상위 N개만 요약 제공
      List<?> candidateSummaries = candidates.stream()
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
          "message", "여러 명이 검색되었습니다. playerId 또는 teamName/position 필터를 추가해주세요.");
    }

    // 6) 상세 조회 (시즌/월별 스탯 포함)
    PlayerDetailResponseDto detail = playerService.getPlayerDetail(chosen.getId());

    // 7) 타자/투수 분기하여 핵심 지표 구성
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

  private Object buildDetailResponseByPlayerId(Long playerId) {
    try {
      PlayerDetailResponseDto detail = playerService.getPlayerDetail(playerId);
      // 플레이어 기본 정보 추출이 필요하여 playerId로 역으로 검색
      // 간단히 이름 검색으로 역매핑 시도 (레포에 단건 조회가 없다면)
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
    return "선수 이름 또는 playerId로 시즌 스탯(타자/투수)을 조회합니다. teamName/position 필터 지원.";
  }

  @Override
  public String getName() {
    return "getPlayerStats";
  }
}
