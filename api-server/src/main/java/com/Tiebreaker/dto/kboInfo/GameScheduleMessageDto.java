package com.Tiebreaker.dto.kboInfo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class GameScheduleMessageDto {
  private String status;
  private long collected_at;
  private String source;
  private String date; // today payload에서만 존재할 수 있음
  private List<GameScheduleItemDto> data; // 월별/오늘 공통
}
