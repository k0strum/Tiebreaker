package com.Tiebreaker.entity.kboInfo;

import com.Tiebreaker.entity.BaseTimeEntity;
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
public class KboConstants extends BaseTimeEntity {

  // KBO 리그 상수 테이블
  @Id
  private Integer year; // 연도를 Primary Key로 사용

  private Double woba; // weighted On Base Average, 가중 출루율
  private Double scale; // weighted On Base Average Scale, 가중 출루율 스케일
  private Double ebb; // effective Based on Balls Event Value, 실질볼넷 이벤트 가치
  private Double singles; // Single Event Value, 1루타 이벤트 가치
  private Double doubles; // Double Event Value, 2루타 이벤트 가치
  private Double triples; // Triple Event Value, 3루타 이벤트 가치
  private Double homeRuns; // Home Run Event Value, 홈런 이벤트 가치
  private Double sb2; // First to second stolen bases Event Value, 2루 도루 이벤트 가치
  private Double sb3; // Second to third stolen bases Event Value, 3루 도루 이벤트 가치
  private Double cs2; // First to second caught stealing Event Value, 2루 도루 실패 이벤트 가치
  private Double cs3; // Second to third caught stealing Event Value, 3루 도루 실패 이벤트 가치
  private Double runsPerEpa; // Runs per effective Plate Appearance, R/ePA
  private Double rpw; // Runs Per Win, 승리당 득점
  private Double cFip; // Season const FIP, 시즌 FIP 상수
}
