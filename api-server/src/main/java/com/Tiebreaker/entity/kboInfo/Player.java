package com.Tiebreaker.entity.kboInfo;

import com.Tiebreaker.constant.PlayerStatus;
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

  @Column(nullable = false)
  private String playerName;

  @Column(nullable = false)
  private String birthday;

  @Column(nullable = false)
  private String heightWeight;

  @Column(nullable = false)
  private String draftRank;

  @Column(nullable = false)
  private Integer backNumber;

  @Column(nullable = false)
  private String position;

  @Column(nullable = false)
  private String career;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private PlayerStatus status;

  private String teamName;
  private Integer game;

  // 1:1 관계 설정. 한 선수는 하나의 타자 기록을 가짐
  @OneToOne(mappedBy = "player", cascade = CascadeType.ALL)
  private BatterStats batterStats;

  // 1:1 관계 설정. 한 선수는 하나의 투수 기록을 가짐
  @OneToOne(mappedBy = "player", cascade = CascadeType.ALL)
  private PitcherStats pitcherStats;
}
