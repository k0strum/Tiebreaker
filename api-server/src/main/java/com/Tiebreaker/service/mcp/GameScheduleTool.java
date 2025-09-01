package com.Tiebreaker.service.mcp;

import com.Tiebreaker.entity.livegame.GameSchedule;
import com.Tiebreaker.service.livegame.GameScheduleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
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

  private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

  @Override
  public Object execute(Map<String, Object> arguments) {
    String argDate = arguments == null ? null : (String) arguments.get("date");
    
    // 날짜 검증 및 기본값 설정
    String date;
    if (argDate == null || argDate.isBlank()) {
      date = LocalDate.now().toString();
    } else {
      try {
        // 날짜 형식 검증
        LocalDate.parse(argDate, DATE_FORMATTER);
        date = argDate;
      } catch (DateTimeParseException e) {
        return Map.of(
            "error", "잘못된 날짜 형식입니다.",
            "detail", "날짜는 YYYY-MM-DD 형식으로 입력해주세요. (예: 2024-08-15)",
            "inputDate", argDate);
      }
    }

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
