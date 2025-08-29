package com.Tiebreaker.service.kafka;

import com.Tiebreaker.dto.livegame.CommentaryEvent;
import com.Tiebreaker.entity.livegame.Commentary;
import com.Tiebreaker.service.livegame.CommentaryService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CommentaryConsumer {

  private final CommentaryService commentaryService;
  private final ObjectMapper objectMapper;

  @KafkaListener(topics = "livegame", groupId = "tiebreaker-livegame")
  public void onMessage(String message) {
    try {
      CommentaryEvent event = objectMapper.readValue(message, CommentaryEvent.class);
      Commentary saved = commentaryService.save(event);
      commentaryService.broadcast(saved);
      System.out.println("✅ 해설 데이터 처리 완료: " + event.getText());
    } catch (JsonProcessingException e) {
      System.err.println("❌ Error parsing - Commentary JSON String: " + e.getMessage());
    } catch (Exception e) {
      System.err.println("❌ Error processing livegame event: " + e.getMessage());
    }
  }
}
