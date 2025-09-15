package com.Tiebreaker.entity.scorebook;

import com.Tiebreaker.constant.AtBatResult;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class AtBat {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "inning_id")
  private Inning inning;

  // --- 타석 기본 정보 ---
  private Integer batterOrder; // 타순 (예: 3번 타자)
  private String batterName;
  private String pitcherName;

  // --- 타석 시작 전 상황 ---
  private Integer outsBefore; // 타석 시작 전 아웃카운트
  private String runnersOnBaseBefore; // 타석 시작 전 주자 상황 (예: "1,3루")

  // --- 타석 결과 ---
  @Enumerated(EnumType.STRING)
  private AtBatResult result; // 타석 결과 (Enum 타입으로 관리)
  private String resultDetail; // 결과 상세 (예: "좌익수 앞 안타", "유격수 땅볼")

  // --- 결과로 인한 변화 ---
  private Integer rbi; // 타점
  private Integer runsScored; // 이 타석으로 인해 발생한 득점
  private String runnersOnBaseAfter; // 타석 종료 후 주자 상황

  // --- 특수 상황 기록 ---
  private String substitution; // 선수 교체 정보 (예: "투수 교체: OOO -> XXX")
  private String specialNote; // 기타 특이사항 (예: "도루", "폭투")
  private String extraInfo; // 추가 정보 (예: "타자 교체", "투수 교체")

}
