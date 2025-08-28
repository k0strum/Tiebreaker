package com.Tiebreaker.service.kafka;

import com.Tiebreaker.dto.kboInfo.LiveGameInfoDto;
import com.Tiebreaker.entity.LiveGameInfo;
import com.Tiebreaker.service.LiveGameInfoService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LiveGameInfoConsumer {

  private final LiveGameInfoService liveGameInfoService;
  private final ObjectMapper objectMapper;

  @KafkaListener(topics = "live-game-info", groupId = "tiebreaker-live-game")
  public void onMessage(String message) {
    try {
      LiveGameInfoDto event = objectMapper.readValue(message, LiveGameInfoDto.class);
      LiveGameInfo saved = liveGameInfoService.save(event);
      liveGameInfoService.broadcast(saved);
      System.out.println("✅ 실시간 경기 정보 처리 완료: " + event.getGameId() + " - " + event.getStatus() + " " + event.getInning()
          + "회" + event.getHalf());
    } catch (JsonProcessingException e) {
      System.err.println("❌ Error parsing - LiveGameInfo JSON String: " + e.getMessage());
    } catch (Exception e) {
      System.err.println("❌ Error processing live game info event: " + e.getMessage());
    }
  }
}
