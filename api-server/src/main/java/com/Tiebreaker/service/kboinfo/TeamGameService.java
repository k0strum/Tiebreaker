package com.Tiebreaker.service.kboinfo;

import com.Tiebreaker.entity.kboInfo.TeamRank;
import com.Tiebreaker.repository.kboInfo.TeamRankRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class TeamGameService {

  private final TeamRankRepository teamRankRepository;

  // KBO 규정 상수
  private static final double BATTER_PLATE_APPEARANCES_MULTIPLIER = 2.0; // 타자 규정타석 = 경기수 × 2.0 (완화)
  private static final double PITCHER_INNINGS_MULTIPLIER = 0.5; // 투수 규정이닝 = 경기수 × 0.5 (완화)

  /**
   * 팀별 경기수 조회
   */
  @Transactional(readOnly = true)
  public Map<String, Integer> getTeamGames() {
    List<TeamRank> teamRanks = teamRankRepository.findAll();
    Map<String, Integer> teamGames = new HashMap<>();

    log.info("TeamGameService - 전체 팀 수: {}", teamRanks.size());

    for (TeamRank teamRank : teamRanks) {
      teamGames.put(teamRank.getTeamName(), teamRank.getPlays());
      log.info("TeamGameService - 팀: {}, 경기수: {}", teamRank.getTeamName(), teamRank.getPlays());
    }

    return teamGames;
  }

  /**
   * 특정 팀의 경기수 조회
   */
  @Transactional(readOnly = true)
  public Integer getTeamGames(String teamName) {
    TeamRank teamRank = teamRankRepository.findByTeamName(teamName);
    return teamRank != null ? teamRank.getPlays() : 0;
  }

  /**
   * 특정 팀의 타자 규정타석 계산
   */
  public int calculateBatterPlateAppearances(String teamName) {
    int games = getTeamGames(teamName);
    return (int) (games * BATTER_PLATE_APPEARANCES_MULTIPLIER);
  }

  /**
   * 특정 팀의 투수 규정이닝 계산
   */
  public double calculatePitcherInnings(String teamName) {
    int games = getTeamGames(teamName);
    return games * PITCHER_INNINGS_MULTIPLIER;
  }

  /**
   * 모든 팀의 타자 규정타석 맵 반환
   */
  public Map<String, Integer> getAllBatterPlateAppearances() {
    Map<String, Integer> result = new HashMap<>();
    Map<String, Integer> teamGames = getTeamGames();

    for (Map.Entry<String, Integer> entry : teamGames.entrySet()) {
      result.put(entry.getKey(), (int) (entry.getValue() * BATTER_PLATE_APPEARANCES_MULTIPLIER));
    }

    return result;
  }

  /**
   * 모든 팀의 투수 규정이닝 맵 반환
   */
  public Map<String, Double> getAllPitcherInnings() {
    Map<String, Double> result = new HashMap<>();
    Map<String, Integer> teamGames = getTeamGames();

    for (Map.Entry<String, Integer> entry : teamGames.entrySet()) {
      result.put(entry.getKey(), entry.getValue() * PITCHER_INNINGS_MULTIPLIER);
    }

    return result;
  }

}
