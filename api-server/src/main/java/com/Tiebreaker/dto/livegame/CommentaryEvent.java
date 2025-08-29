package com.Tiebreaker.dto.livegame;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CommentaryEvent {
  private String gameId;
  private Long ts;
  private String text;
  private String severity; // HIGHLIGHT/INFO
  private Integer inning;
  private String half; // T/B
}
