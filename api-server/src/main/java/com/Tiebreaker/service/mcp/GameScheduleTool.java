package com.Tiebreaker.service.mcp;

import com.Tiebreaker.entity.livegame.GameSchedule;
import com.Tiebreaker.service.livegame.GameScheduleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 경기 일정 조회 MCP 도구
 * 사용 예: "오늘 경기 일정 알려줘", "2024-08-15 경기 일정"
 */
@Component
public class GameScheduleTool implements McpTool {

  @Autowired
  private GameScheduleService scheduleService;

  @Override
  public Object execute(Map<String, Object> arguments) {
    String argDate = arguments == null ? null : (String) arguments.get("date");
    String date = (argDate == null || argDate.isBlank()) ? LocalDate.now().toString() : argDate;

    try {
      List<GameSchedule> schedules = (argDate == null || argDate.isBlank())
          ? scheduleService.getToday()
          : scheduleService.getByDate(date);

      if (schedules == null) {
        schedules = Collections.emptyList();
      }

      return Map.of(
          "date", date,
          "count", schedules.size(),
          "schedules", schedules);
    } catch (Exception e) {
      return Map.of(
          "date", date,
          "error", "경기 일정 조회 중 오류",
          "detail", e.getMessage());
    }
  }

  @Override
  public String getDescription() {
    return "경기 일정을 조회합니다. date(YYYY-MM-DD)가 없으면 오늘 일정 반환.";
  }

  @Override
  public String getName() {
    return "getGameSchedule";
  }
}
