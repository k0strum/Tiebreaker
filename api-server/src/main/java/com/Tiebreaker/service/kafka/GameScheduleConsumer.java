package com.Tiebreaker.service.kafka;

import com.Tiebreaker.dto.livegame.GameScheduleItemDto;
import com.Tiebreaker.dto.kboInfo.GameScheduleMessageDto;
import com.Tiebreaker.entity.livegame.GameSchedule;
import com.Tiebreaker.repository.GameScheduleRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class GameScheduleConsumer {

  private final ObjectMapper objectMapper;
  private final GameScheduleRepository repository;

  @KafkaListener(topics = "kbo-game-schedule", groupId = "tiebreaker-schedule")
  public void onMonthlySchedule(String message) {
    saveMessagePayload(message);
  }

  @KafkaListener(topics = "kbo-today-games", groupId = "tiebreaker-schedule")
  public void onTodayGames(String message) {
    saveMessagePayload(message);
  }

  @Transactional
  private void saveMessagePayload(String message) {
    try {
      GameScheduleMessageDto payload = objectMapper.readValue(message, GameScheduleMessageDto.class);
      int saved = 0;
      if (payload.getData() != null) {
        for (GameScheduleItemDto dto : payload.getData()) {
          GameSchedule entity = repository.findById(dto.getGameId()).orElseGet(GameSchedule::new);
          mapToEntity(dto, entity);
          repository.save(entity);
          saved++;
        }
      }
      System.out.println("✅ 저장 완료: " + saved + "건");
    } catch (Exception e) {
      System.err.println("❌ GameSchedule 처리 오류: " + e.getMessage());
    }
  }

  private void mapToEntity(GameScheduleItemDto dto, GameSchedule e) {
    e.setGameId(dto.getGameId());
    e.setGameDate(dto.getGameDate());
    e.setGameDateTime(dto.getGameDateTime());
    e.setStadium(dto.getStadium());
    e.setHomeTeamCode(dto.getHomeTeamCode());
    e.setHomeTeamName(dto.getHomeTeamName());
    e.setHomeTeamScore(dto.getHomeTeamScore());
    e.setAwayTeamCode(dto.getAwayTeamCode());
    e.setAwayTeamName(dto.getAwayTeamName());
    e.setAwayTeamScore(dto.getAwayTeamScore());
    e.setStatusCode(dto.getStatusCode());
    e.setStatusInfo(dto.getStatusInfo()); // statusInfo 추가
    e.setWinner(dto.getWinner());
    e.setSuspended(dto.getSuspended());
    e.setBroadChannel(dto.getBroadChannel());
    e.setHomeStarterName(dto.getHomeStarterName());
    e.setAwayStarterName(dto.getAwayStarterName());
    e.setRoundCode(dto.getRoundCode());
  }
}
