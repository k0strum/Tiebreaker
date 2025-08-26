package com.Tiebreaker.service.kafka;

import com.Tiebreaker.dto.commentary.CommentaryEvent;
import com.Tiebreaker.entity.Commentary;
import com.Tiebreaker.service.CommentaryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CommentaryConsumer {

  private final CommentaryService commentaryService;

  @KafkaListener(topics = "commentary", groupId = "tiebreaker-commentary")
  public void onMessage(@Payload CommentaryEvent event) {
    try {
      Commentary saved = commentaryService.save(event);
      commentaryService.broadcast(saved);
    } catch (Exception e) {
      log.error("Failed to process commentary event: {}", event, e);
    }
  }
}
