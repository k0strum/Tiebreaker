package com.Tiebreaker.entity.scorebook;

import com.Tiebreaker.constant.HalfInning;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Inning {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "game_id")
  private Game game;

  @Column(nullable = false)
  private Integer inningNumber;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private HalfInning halfinning;

  private Integer runsScored; // 해당 이닝에 난 점수
  private Integer hits;       // 해당 이닝의 안타 수
  private Integer errors;     // 해당 이닝의 에러 수
  private Integer leftOnBase; // 잔루

  @OneToMany(mappedBy = "inning", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<AtBat> atBats = new ArrayList<>();

}
