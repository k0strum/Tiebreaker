package com.Tiebreaker.service.kboInfoCollecting;

import com.Tiebreaker.dto.kboInfo.KboRankDto;
import com.Tiebreaker.dto.kboInfo.PlayerDto;
import com.Tiebreaker.service.kboInfoCollecting.daily.RankCollectService;
import com.Tiebreaker.service.kboInfoCollecting.daily.PlayerDataCollectService;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

@Service
@RequiredArgsConstructor
public class KafkaConsumerService {

  private final ObjectMapper objectMapper;
  private final RankCollectService rankCollectService;
  private final PlayerDataCollectService playerDataCollectService;

  @KafkaListener(topics = "kbo-team-rank-data", groupId = "tiebreak-group")
  public void consumeRankData(String message) {
    try {
      KboRankDto kboRankDto = objectMapper.readValue(message, KboRankDto.class);

      // DB 저장을 위해 RankService의 메서드 호출
      rankCollectService.updateRanks(kboRankDto);
    } catch (JsonProcessingException e) {
      System.err.println("Error parsing - Team Rank JSON String: " + e);
    }
  }

  @KafkaListener(topics = "kbo-player-data", groupId = "tiebreak-group")
  public void consumePlayerData(String message) {
    try {
      PlayerDto playerDto = objectMapper.readValue(message, PlayerDto.class);

      // DB 저장을 위해 PlayerDataService의 메서드 호출
      playerDataCollectService.savePlayerData(playerDto);

      System.out.println("✅ 선수 데이터 저장 완료: " + playerDto.getPlayerName() + " (ID: " + playerDto.getId() + ")");
    } catch (JsonProcessingException e) {
      System.err.println("❌ Error parsing - Player JSON String: " + e.getMessage());
    } catch (Exception e) {
      System.err.println("❌ Error saving player data: " + e.getMessage());
    }
  }

}
