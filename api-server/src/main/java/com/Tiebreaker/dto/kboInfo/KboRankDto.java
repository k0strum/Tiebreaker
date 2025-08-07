package com.Tiebreaker.dto.kboInfo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class KboRankDto {
  @JsonProperty("collected_at")
  private double collectedAt;

  private String source;
  private List<TeamRankDto> data;
}
