package com.Tiebreaker.dto.kboInfo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 선수 랭킹 정보를 담는 DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PlayerRankingDto {

  private Integer rank; // 순위
  private Long playerId; // 선수 ID
  private String playerName; // 선수명
  private String teamName; // 팀명
  private String imageUrl; // 선수 이미지 URL

  // 타자 랭킹용 필드
  private Double battingAverage; // 타율
  private Integer homeRuns; // 홈런
  private Integer runsBattedIn; // 타점
  private Double onBasePercentage; // 출루율
  private Double ops; // OPS
  private Integer stolenBases; // 도루

  // 투수 랭킹용 필드
  private Integer wins; // 승수
  private Integer saves; // 세이브
  private Integer holds; // 홀드
  private Integer strikeoutsPitched; // 탈삼진
  private Double earnedRunAverage; // 방어율
  private Double whip; // WHIP

  // 랭킹 타입 구분
  private String rankingType; // "BATTING_AVERAGE", "HOME_RUNS", "ERA" 등
}
