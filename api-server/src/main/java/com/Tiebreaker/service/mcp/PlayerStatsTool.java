// package com.Tiebreaker.service.mcp;
//
// import com.Tiebreaker.service.kboinfo.PlayerService;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.stereotype.Component;
//
// import java.util.Map;
//
/// **
// * 선수 성적 조회 MCP 도구
// * 사용 예: "김현수 선수 올해 성적 어때?"
// */
// @Component
// public class PlayerStatsTool implements McpTool {
//
// @Autowired
// private PlayerService playerService;
//
// @Override
// public Object execute(Map<String, Object> arguments) {
// try {
// String playerName = (String) arguments.get("playerName");
//
// if (playerName == null || playerName.trim().isEmpty()) {
// return Map.of(
// "error", "선수 이름이 필요합니다.",
// "example", "getPlayerStats({\"playerName\": \"김현수\"})");
// }
//
// // 실제 선수 데이터 조회
// var playerStats = playerService.getPlayerStatsByName(playerName);
//
// if (playerStats == null) {
// return Map.of(
// "error", "선수를 찾을 수 없습니다: " + playerName,
// "suggestion", "정확한 선수 이름을 입력해주세요.");
// }
//
// return Map.of(
// "playerName", playerName,
// "stats", playerStats,
// "message", String.format(
// "%s 선수의 2024시즌 성적입니다:\n" +
// "• 타율: %s\n" +
// "• 홈런: %s개\n" +
// "• 타점: %s개\n" +
// "• 출루율: %s\n" +
// "• 장타율: %s",
// playerName,
// playerStats.getBattingAverage(),
// playerStats.getHomeRuns(),
// playerStats.getRbi(),
// playerStats.getOnBasePercentage(),
// playerStats.getSluggingPercentage()));
//
// } catch (Exception e) {
// return Map.of(
// "error", "선수 성적 조회 중 오류가 발생했습니다: " + e.getMessage());
// }
// }
//
// @Override
// public String getDescription() {
// return "KBO 선수의 현재 시즌 성적을 조회합니다. 선수 이름을 입력하면 타율, 홈런, 타점 등의 정보를 제공합니다.";
// }
//
// @Override
// public String getName() {
// return "getPlayerStats";
// }
// }
