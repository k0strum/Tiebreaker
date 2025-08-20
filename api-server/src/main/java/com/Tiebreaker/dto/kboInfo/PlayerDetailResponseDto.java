package com.Tiebreaker.dto.kboInfo;

import com.Tiebreaker.constant.PlayerType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PlayerDetailResponseDto {
  // 기본 정보
  private Long id;
  private String playerName;
  private String teamName;
  private String position;
  private String backNumber;
  private String birthday;
  private String heightWeight;
  private String draftRank;
  private String career;
  private String imageUrl;
  private PlayerType playerType;

  // 타자 기록
  private BatterStatsDto batterStats;
  private List<BatterMonthlyStatsDto> batterMonthlyStats;

  // 투수 기록
  private PitcherStatsDto pitcherStats;
  private List<PitcherMonthlyStatsDto> pitcherMonthlyStats;

  // 리그 상수
  private KboConstantsDto kboConstants;
}
