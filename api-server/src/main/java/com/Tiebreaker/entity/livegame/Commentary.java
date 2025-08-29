package com.Tiebreaker.entity.livegame;

import com.Tiebreaker.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "livegame", indexes = {
    @Index(name = "idx_commentary_game_ts", columnList = "gameId, ts")
})
public class Commentary extends BaseTimeEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  // 우리 시스템의 경기 식별자(예: KBO-YYYY-MM-DD-홈-원정)
  @Column(nullable = false, length = 100)
  private String gameId;

  // 이벤트 발생 시각(Epoch millis)
  @Column(nullable = false)
  private Long ts; // epoch millis

  // 사용자에게 표시할 문자중계 문장
  @Column(nullable = false, length = 2000)
  private String text;

  // 강조 레벨(예: HIGHLIGHT/INFO 등)
  @Column(length = 20)
  private String severity; // HIGHLIGHT / INFO 등

  // 이닝 번호(1,2,...)
  @Column
  private Integer inning;

  // 이닝의 상/하(T: 초, B: 말)
  @Column(length = 1)
  private String half; // T/B
}
