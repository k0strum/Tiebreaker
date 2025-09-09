package com.Tiebreaker.entity.scorebook;

import com.Tiebreaker.constant.GameStatus;
import com.Tiebreaker.entity.auth.Member;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
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
    private LocalDateTime gameDate;

    @Column(nullable = false)
    private String stadium;

    @Column(nullable = false)
    private String homeTeamStartingPitcher;

    @Column(nullable = false)
    private String awayTeamStartingPitcher;

    @Enumerated(EnumType.STRING)
    private GameStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @OneToOne(mappedBy = "game", cascade = CascadeType.ALL)
    private GameDetail gameDetail;

    @OneToMany(mappedBy = "game", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Inning> innings = new ArrayList<>();

}
