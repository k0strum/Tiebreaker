package com.Tiebreaker.dto.kboInfo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Kafka로부터 선수 한 명의 마스터 정보와 스탯 정보를 받는 최상위 DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PlayerDto {

  // === Player Entity에 해당하는 고유 정보 ===
  private Long id; // KBO 선수 고유 ID (p_no)
  private String playerName;
  private String birthday;
  private String heightWeight;
  private String draftRank;
  private Integer backNumber;
  private String position;
  private String career;
  private String teamName;

  // === 하위 스탯 정보 ===
  // 해당 선수의 타자 기록 (타자가 아니면 이 부분은 null)
  private BatterStatsDto batterStats;

  // 해당 선수의 투수 기록 (투수가 아니면 이 부분은 null)
  private PitcherStatsDto pitcherStats;
}