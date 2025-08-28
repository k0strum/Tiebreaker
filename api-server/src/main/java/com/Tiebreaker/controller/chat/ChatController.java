package com.Tiebreaker.controller.chat;

import com.Tiebreaker.dto.chat.ChatMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class ChatController {

  private final SimpMessagingTemplate messagingTemplate;

  @MessageMapping("/chat.send")
  public void send(@Payload ChatMessage message) {
    // gameId 별 주제로 브로드캐스트
    String destination = "/topic/chat." + message.getGameId();
    messagingTemplate.convertAndSend(destination, message);
  }
}
