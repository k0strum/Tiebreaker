package com.Tiebreaker.entity.scorebook;

import com.Tiebreaker.entity.auth.Member;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Game {

    // 스코어북의 경기 전반 기록(썸네일용)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String homeTeam;

    @Column(nullable = false)
    private String awayTeam;

    @Column(nullable = false)
    private Integer homeScore;

    @Column(nullable = false)
    private Integer awayScore;

    @Column(nullable = false)
    private LocalDateTime gameDate;

    @Column(nullable = false)
    private String stadium;

    @Column(nullable = false)
    private String homeTeamStartingPitcher;

    @Column(nullable = false)
    private String awayTeamStartingPitcher;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

}
