package com.Tiebreaker.dto.kboInfo;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PlayerMonthlyStatsMessage {

  private Long playerId;

  // 둘 다 nullable. 크롤러는 선수 유형에 따라 한쪽만 채움 가능
  private List<BatterMonthlyStatsDto> batter;
  private List<PitcherMonthlyStatsDto> pitcher;
}
