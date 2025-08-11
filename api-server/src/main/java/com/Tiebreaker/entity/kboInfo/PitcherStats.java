package com.Tiebreaker.entity.kboInfo;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PitcherStats {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "player_id")
  private Player player;

  // 승리 / 패배 / 세이브 / 블론세이브 / 홀드
  private Integer win;
  private Integer lose;
  private Integer save;
  private Integer blownSave;
  private Integer hold;
  // 완투 / 완볼
  private Integer completeGame;
  private Integer shutout;
  // 상대 타자 수
  private Integer batterFaced;
  // 투구 수
  private Integer pitchCount;
  // 이닝
  private Integer innings;
  // 허용 안타 / 2루타 / 3루타 / 홈런
  private Integer hits;
  private Integer doubles;
  private Integer triples;
  private Integer homeRuns;
  // 사구 / 실점 / 자책점
  private Integer baseOnBalls;
  private Integer runs;
  private Integer earnedRuns;
  // 삼진 / 퀄리티스타트
  private Integer strikeOut;
  private Integer qualityStart;
}
