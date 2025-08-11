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
    private Integer rank;
    private String teamName;
    private Integer plays;
    private Integer wins;
    private Integer losses;
    private Integer draws;
    private Double winRate;
    private Double gameBehind;
    private String streak;
}
