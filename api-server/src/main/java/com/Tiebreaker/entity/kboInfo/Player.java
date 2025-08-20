package com.Tiebreaker.entity.kboInfo;

import com.Tiebreaker.constant.PlayerStatus;
import com.Tiebreaker.constant.PlayerType;
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
public class Player {

  @Id
  private Long id;

  private String imageUrl; // 선수 이미지 URL

  private String sourceImageUrl; // 원본(크롤링) 이미지 URL - 변경 여부 비교용

  @Column(nullable = false)
  private String playerName; // 선수명

  @Column(nullable = true)
  private String birthday; // 생년월일

  @Column(nullable = true)
  private String heightWeight; // 신체조건 (키/몸무게)

  @Column(nullable = true)
  private String draftRank; // 지명순위

  private String backNumber; // 등번호

  @Column(nullable = true)
  private String position; // 포지션

  @Column(nullable = true)
  private String career; // 경력

  @Column(nullable = false)
  private String teamName; // 팀명

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private PlayerStatus status; // 선수 상태

  @OneToOne(mappedBy = "player", cascade = CascadeType.ALL)
  private BatterStats batterStats; // 1:1 관계 설정. 한 선수는 하나의 타자 기록을 가짐

  @OneToOne(mappedBy = "player", cascade = CascadeType.ALL)
  private PitcherStats pitcherStats; // 1:1 관계 설정. 한 선수는 하나의 투수 기록을 가짐

  @Enumerated(EnumType.STRING)
  private PlayerType playerType = PlayerType.NONE; // 플레이어 타자 / 투수 / 투타겸업 / 기록없음 분류

}
