package com.Tiebreaker.dto.kboInfo;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CurrentTeamRankResponseDto {
    private LocalDateTime regDate;
    private LocalDateTime updateDate;
    private String teamLogoUrl;
    private int rank;
    private String teamName;
    private int plays;
    private int wins;
    private int losses;
    private int draws;
    private double winRate;
    private double gameBehind;
    private String streak;
}
