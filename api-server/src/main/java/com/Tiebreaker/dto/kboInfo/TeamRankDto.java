package com.Tiebreaker.dto.kboInfo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TeamRankDto {
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
