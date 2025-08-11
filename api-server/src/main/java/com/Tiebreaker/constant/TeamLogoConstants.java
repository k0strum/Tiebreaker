package com.Tiebreaker.constant;

import java.util.Map;

public class TeamLogoConstants {
    
    public static final Map<String, String> TEAM_LOGOS = Map.of(
        "SSG", "/images/teams/ssg.png",
        "키움", "/images/teams/kiwoom.png",
        "LG", "/images/teams/lg.png",
        "KT", "/images/teams/kt.png",
        "KIA", "/images/teams/kia.png",
        "NC", "/images/teams/nc.png",
        "삼성", "/images/teams/samsung.png",
        "롯데", "/images/teams/lotte.png",
        "두산", "/images/teams/doosan.png",
        "한화", "/images/teams/hanwha.png"
    );
    
    public static final String DEFAULT_LOGO = "/images/teams/default.png";
    
    public static String getTeamLogo(String teamName) {
        return TEAM_LOGOS.getOrDefault(teamName, DEFAULT_LOGO);
    }
}
