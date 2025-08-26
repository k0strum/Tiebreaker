package com.Tiebreaker.dto.commentary;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentaryEvent {
  private String gameId;
  private Long ts;
  private String text;
  private String severity; // HIGHLIGHT/INFO
  private Integer inning;
  private String half; // T/B
}
