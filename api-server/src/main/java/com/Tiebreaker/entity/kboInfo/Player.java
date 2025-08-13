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

  private String imageUrl;

  @Column(nullable = false)
  private String playerName;

  @Column(nullable = false)
  private String birthday;

  @Column(nullable = false)
  private String heightWeight;

  @Column(nullable = false)
  private String draftRank;

  private String backNumber;

  @Column(nullable = false)
  private String position;

  @Column(nullable = false)
  private String career;

  @Column(nullable = false)
  private String teamName;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private PlayerStatus status;

  // 1:1 관계 설정. 한 선수는 하나의 타자 기록을 가짐
  @OneToOne(mappedBy = "player", cascade = CascadeType.ALL)
  private BatterStats batterStats;

  // 1:1 관계 설정. 한 선수는 하나의 기록 지표를 가짐(테스트용)
  @OneToOne(mappedBy = "player", cascade = CascadeType.ALL)
  private BatterCalculatedStats batterCalculatedStatus;

  // 1:1 관계 설정. 한 선수는 하나의 투수 기록을 가짐
  @OneToOne(mappedBy = "player", cascade = CascadeType.ALL)
  private PitcherStats pitcherStats;

  // 1:1 관계 설정. 한 선수는 하나의 기록 지표를 가짐(테스트용)
  @OneToOne(mappedBy = "player", cascade = CascadeType.ALL)
  private PitcherCalculatedStats pitcherCalculatedStats;

  // 플레이어 타자 / 투수 / 투타겸업 / 기록없음 분류
  @Enumerated(EnumType.STRING)
  private PlayerType playerType = PlayerType.NONE;

}
