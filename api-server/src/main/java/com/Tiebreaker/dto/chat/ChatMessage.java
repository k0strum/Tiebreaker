package com.Tiebreaker.dto.chat;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage {
  private String gameId;
  private String sender;
  private String content;
  private long ts;
}
