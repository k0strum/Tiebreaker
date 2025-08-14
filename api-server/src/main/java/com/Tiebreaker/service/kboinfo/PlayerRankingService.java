package com.Tiebreaker.service.kboinfo;

import com.Tiebreaker.dto.kboInfo.PlayerRankingDto;
import com.Tiebreaker.entity.kboInfo.BatterStats;
import com.Tiebreaker.entity.kboInfo.BatterCalculatedStats;
import com.Tiebreaker.entity.kboInfo.PitcherStats;
import com.Tiebreaker.entity.kboInfo.PitcherCalculatedStats;
import com.Tiebreaker.repository.kboInfo.BatterStatsRepository;
import com.Tiebreaker.repository.kboInfo.BatterCalculatedStatsRepository;
import com.Tiebreaker.repository.kboInfo.PitcherStatsRepository;
import com.Tiebreaker.repository.kboInfo.PitcherCalculatedStatsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
@Slf4j
public class PlayerRankingService {

  private final BatterStatsRepository batterStatsRepository;
  private final BatterCalculatedStatsRepository batterCalculatedStatsRepository;
  private final PitcherStatsRepository pitcherStatsRepository;
  private final PitcherCalculatedStatsRepository pitcherCalculatedStatsRepository;
  private final TeamGameService teamGameService;

  /**
   * 타율 순위 조회 (규정 타석 필터링 적용)
   */
  @Transactional(readOnly = true)
  public List<PlayerRankingDto> getBattingAverageRanking() {
    // 모든 타자 계산 통계 조회
    List<BatterCalculatedStats> allStats = batterCalculatedStatsRepository.findBattingAverageRanking();

    // 팀별 규정타석 기준 가져오기
    Map<String, Integer> teamPlateAppearancesCriteria = teamGameService.getAllBatterPlateAppearances();

    // 규정타석 이상인 선수들만 필터링
    List<PlayerRankingDto> rankings = new ArrayList<>();

    for (BatterCalculatedStats stat : allStats) {
      String teamName = stat.getPlayer().getTeamName();
      Integer minPlateAppearances = teamPlateAppearancesCriteria.get(teamName);

      // null 체크 추가
      if (stat.getPlayer().getBatterStats() == null) {
        continue;
      }

      Integer playerPlateAppearances = stat.getPlayer().getBatterStats().getPlateAppearances();

      if (playerPlateAppearances == null) {
        continue;
      }

      if (minPlateAppearances != null
          && playerPlateAppearances >= minPlateAppearances) {
        PlayerRankingDto dto = new PlayerRankingDto();
        dto.setPlayerId(stat.getPlayer().getId());
        dto.setPlayerName(stat.getPlayer().getPlayerName());
        dto.setTeamName(teamName);
        dto.setImageUrl(stat.getPlayer().getImageUrl());
        dto.setBattingAverage(stat.getBattingAverage());
        dto.setRankingType("BATTING_AVERAGE");
        rankings.add(dto);
      }
    }

    // 타율 기준으로 정렬
    rankings.sort((a, b) -> Double.compare(b.getBattingAverage(), a.getBattingAverage()));

    // 순위 설정
    for (int i = 0; i < rankings.size(); i++) {
      rankings.get(i).setRank(i + 1);
    }

    return rankings;
  }

  /**
   * 홈런 순위 조회
   */
  @Transactional(readOnly = true)
  public List<PlayerRankingDto> getHomeRunRanking() {
    List<BatterStats> stats = batterStatsRepository.findHomeRunRanking();

    return IntStream.range(0, stats.size())
        .mapToObj(i -> {
          BatterStats stat = stats.get(i);
          PlayerRankingDto dto = new PlayerRankingDto();
          dto.setRank(i + 1);
          dto.setPlayerId(stat.getPlayer().getId());
          dto.setPlayerName(stat.getPlayer().getPlayerName());
          dto.setTeamName(stat.getPlayer().getTeamName());
          dto.setImageUrl(stat.getPlayer().getImageUrl());
          dto.setHomeRuns(stat.getHomeRuns());
          dto.setRankingType("HOME_RUNS");
          return dto;
        })
        .collect(Collectors.toList());
  }

  /**
   * 타점 순위 조회
   */
  @Transactional(readOnly = true)
  public List<PlayerRankingDto> getRbiRanking() {
    List<BatterStats> stats = batterStatsRepository.findRbiRanking();

    return IntStream.range(0, stats.size())
        .mapToObj(i -> {
          BatterStats stat = stats.get(i);
          PlayerRankingDto dto = new PlayerRankingDto();
          dto.setRank(i + 1);
          dto.setPlayerId(stat.getPlayer().getId());
          dto.setPlayerName(stat.getPlayer().getPlayerName());
          dto.setTeamName(stat.getPlayer().getTeamName());
          dto.setImageUrl(stat.getPlayer().getImageUrl());
          dto.setRunsBattedIn(stat.getRunsBattedIn());
          dto.setRankingType("RBI");
          return dto;
        })
        .collect(Collectors.toList());
  }

  /**
   * 출루율 순위 조회 (규정 타석 필터링 적용)
   */
  @Transactional(readOnly = true)
  public List<PlayerRankingDto> getOnBasePercentageRanking() {
    // 모든 타자 계산 통계 조회
    List<BatterCalculatedStats> allStats = batterCalculatedStatsRepository.findOnBasePercentageRanking();

    // 팀별 규정타석 기준 가져오기
    Map<String, Integer> teamPlateAppearancesCriteria = teamGameService.getAllBatterPlateAppearances();

    // 규정타석 이상인 선수들만 필터링
    List<PlayerRankingDto> rankings = new ArrayList<>();

    for (BatterCalculatedStats stat : allStats) {
      String teamName = stat.getPlayer().getTeamName();
      Integer minPlateAppearances = teamPlateAppearancesCriteria.get(teamName);

      // null 체크 추가
      if (stat.getPlayer().getBatterStats() == null) {
        continue;
      }

      Integer playerPlateAppearances = stat.getPlayer().getBatterStats().getPlateAppearances();

      if (playerPlateAppearances == null) {
        continue;
      }

      if (minPlateAppearances != null
          && playerPlateAppearances >= minPlateAppearances) {
        PlayerRankingDto dto = new PlayerRankingDto();
        dto.setPlayerId(stat.getPlayer().getId());
        dto.setPlayerName(stat.getPlayer().getPlayerName());
        dto.setTeamName(teamName);
        dto.setImageUrl(stat.getPlayer().getImageUrl());
        dto.setOnBasePercentage(stat.getOnBasePercentage());
        dto.setRankingType("ON_BASE_PERCENTAGE");
        rankings.add(dto);
      }
    }

    // 출루율 기준으로 정렬
    rankings.sort((a, b) -> Double.compare(b.getOnBasePercentage(), a.getOnBasePercentage()));

    // 순위 설정
    for (int i = 0; i < rankings.size(); i++) {
      rankings.get(i).setRank(i + 1);
    }

    return rankings;
  }

  /**
   * OPS 순위 조회 (규정 타석 필터링 적용)
   */
  @Transactional(readOnly = true)
  public List<PlayerRankingDto> getOpsRanking() {
    // 모든 타자 계산 통계 조회
    List<BatterCalculatedStats> allStats = batterCalculatedStatsRepository.findOpsRanking();

    // 팀별 규정타석 기준 가져오기
    Map<String, Integer> teamPlateAppearancesCriteria = teamGameService.getAllBatterPlateAppearances();

    // 규정타석 이상인 선수들만 필터링
    List<PlayerRankingDto> rankings = new ArrayList<>();

    for (BatterCalculatedStats stat : allStats) {
      String teamName = stat.getPlayer().getTeamName();
      Integer minPlateAppearances = teamPlateAppearancesCriteria.get(teamName);

      // null 체크 추가
      if (stat.getPlayer().getBatterStats() == null) {
        continue;
      }

      Integer playerPlateAppearances = stat.getPlayer().getBatterStats().getPlateAppearances();

      if (playerPlateAppearances == null) {
        continue;
      }

      if (minPlateAppearances != null
          && playerPlateAppearances >= minPlateAppearances) {
        PlayerRankingDto dto = new PlayerRankingDto();
        dto.setPlayerId(stat.getPlayer().getId());
        dto.setPlayerName(stat.getPlayer().getPlayerName());
        dto.setTeamName(teamName);
        dto.setImageUrl(stat.getPlayer().getImageUrl());
        dto.setOps(stat.getOps());
        dto.setRankingType("OPS");
        rankings.add(dto);
      }
    }

    // OPS 기준으로 정렬
    rankings.sort((a, b) -> Double.compare(b.getOps(), a.getOps()));

    // 순위 설정
    for (int i = 0; i < rankings.size(); i++) {
      rankings.get(i).setRank(i + 1);
    }

    return rankings;
  }

  /**
   * 도루 순위 조회
   */
  @Transactional(readOnly = true)
  public List<PlayerRankingDto> getStolenBasesRanking() {
    List<BatterStats> stats = batterStatsRepository.findStolenBasesRanking();

    return IntStream.range(0, stats.size())
        .mapToObj(i -> {
          BatterStats stat = stats.get(i);
          PlayerRankingDto dto = new PlayerRankingDto();
          dto.setRank(i + 1);
          dto.setPlayerId(stat.getPlayer().getId());
          dto.setPlayerName(stat.getPlayer().getPlayerName());
          dto.setTeamName(stat.getPlayer().getTeamName());
          dto.setImageUrl(stat.getPlayer().getImageUrl());
          dto.setStolenBases(stat.getStolenBases());
          dto.setRankingType("STOLEN_BASES");
          return dto;
        })
        .collect(Collectors.toList());
  }

  /**
   * 승수 순위 조회
   */
  @Transactional(readOnly = true)
  public List<PlayerRankingDto> getWinsRanking() {
    List<PitcherStats> stats = pitcherStatsRepository.findWinsRanking();

    return IntStream.range(0, stats.size())
        .mapToObj(i -> {
          PitcherStats stat = stats.get(i);
          PlayerRankingDto dto = new PlayerRankingDto();
          dto.setRank(i + 1);
          dto.setPlayerId(stat.getPlayer().getId());
          dto.setPlayerName(stat.getPlayer().getPlayerName());
          dto.setTeamName(stat.getPlayer().getTeamName());
          dto.setImageUrl(stat.getPlayer().getImageUrl());
          dto.setWins(stat.getWins());
          dto.setRankingType("WINS");
          return dto;
        })
        .collect(Collectors.toList());
  }

  /**
   * 세이브 순위 조회
   */
  @Transactional(readOnly = true)
  public List<PlayerRankingDto> getSavesRanking() {
    List<PitcherStats> stats = pitcherStatsRepository.findSavesRanking();

    return IntStream.range(0, stats.size())
        .mapToObj(i -> {
          PitcherStats stat = stats.get(i);
          PlayerRankingDto dto = new PlayerRankingDto();
          dto.setRank(i + 1);
          dto.setPlayerId(stat.getPlayer().getId());
          dto.setPlayerName(stat.getPlayer().getPlayerName());
          dto.setTeamName(stat.getPlayer().getTeamName());
          dto.setImageUrl(stat.getPlayer().getImageUrl());
          dto.setSaves(stat.getSaves());
          dto.setRankingType("SAVES");
          return dto;
        })
        .collect(Collectors.toList());
  }

  /**
   * 홀드 순위 조회
   */
  @Transactional(readOnly = true)
  public List<PlayerRankingDto> getHoldsRanking() {
    List<PitcherStats> stats = pitcherStatsRepository.findHoldsRanking();

    return IntStream.range(0, stats.size())
        .mapToObj(i -> {
          PitcherStats stat = stats.get(i);
          PlayerRankingDto dto = new PlayerRankingDto();
          dto.setRank(i + 1);
          dto.setPlayerId(stat.getPlayer().getId());
          dto.setPlayerName(stat.getPlayer().getPlayerName());
          dto.setTeamName(stat.getPlayer().getTeamName());
          dto.setImageUrl(stat.getPlayer().getImageUrl());
          dto.setHolds(stat.getHolds());
          dto.setRankingType("HOLDS");
          return dto;
        })
        .collect(Collectors.toList());
  }

  /**
   * 탈삼진 순위 조회
   */
  @Transactional(readOnly = true)
  public List<PlayerRankingDto> getStrikeoutsRanking() {
    List<PitcherStats> stats = pitcherStatsRepository.findStrikeoutsRanking();

    return IntStream.range(0, stats.size())
        .mapToObj(i -> {
          PitcherStats stat = stats.get(i);
          PlayerRankingDto dto = new PlayerRankingDto();
          dto.setRank(i + 1);
          dto.setPlayerId(stat.getPlayer().getId());
          dto.setPlayerName(stat.getPlayer().getPlayerName());
          dto.setTeamName(stat.getPlayer().getTeamName());
          dto.setImageUrl(stat.getPlayer().getImageUrl());
          dto.setStrikeoutsPitched(stat.getStrikeouts());
          dto.setRankingType("STRIKEOUTS");
          return dto;
        })
        .collect(Collectors.toList());
  }

  /**
   * 방어율 순위 조회 (규정 이닝 필터링 적용)
   */
  @Transactional(readOnly = true)
  public List<PlayerRankingDto> getEraRanking() {
    // 모든 투수 계산 통계 조회
    List<PitcherCalculatedStats> allStats = pitcherCalculatedStatsRepository.findEraRanking();

    // 팀별 규정이닝 기준 가져오기
    Map<String, Double> teamInningsCriteria = teamGameService.getAllPitcherInnings();

    // 규정이닝 이상인 선수들만 필터링
    List<PlayerRankingDto> rankings = new ArrayList<>();

    for (PitcherCalculatedStats stat : allStats) {
      String teamName = stat.getPlayer().getTeamName();
      Double minInnings = teamInningsCriteria.get(teamName);

      // 직접 PitcherStats 조회
      Optional<PitcherStats> pitcherStatsOpt = pitcherStatsRepository.findByPlayerId(stat.getPlayer().getId());

      if (pitcherStatsOpt.isEmpty()) {
        continue;
      }

      PitcherStats pitcherStats = pitcherStatsOpt.get();
      Double playerInnings = pitcherStats.getInningsPitched();

      if (playerInnings == null || playerInnings == 0.0) {
        continue;
      }

      if (minInnings != null && playerInnings >= minInnings) {
        PlayerRankingDto dto = new PlayerRankingDto();
        dto.setPlayerId(stat.getPlayer().getId());
        dto.setPlayerName(stat.getPlayer().getPlayerName());
        dto.setTeamName(teamName);
        dto.setImageUrl(stat.getPlayer().getImageUrl());
        dto.setEarnedRunAverage(stat.getEarnedRunAverage());
        dto.setRankingType("ERA");
        rankings.add(dto);
      }
    }

    // 방어율 기준으로 정렬 (낮을수록 좋음)
    rankings.sort((a, b) -> Double.compare(a.getEarnedRunAverage(), b.getEarnedRunAverage()));

    // 순위 설정
    for (int i = 0; i < rankings.size(); i++) {
      rankings.get(i).setRank(i + 1);
    }

    return rankings;
  }

  /**
   * WHIP 순위 조회 (규정 이닝 필터링 적용)
   */
  @Transactional(readOnly = true)
  public List<PlayerRankingDto> getWhipRanking() {
    // 모든 투수 계산 통계 조회
    List<PitcherCalculatedStats> allStats = pitcherCalculatedStatsRepository.findWhipRanking();

    // 팀별 규정이닝 기준 가져오기
    Map<String, Double> teamInningsCriteria = teamGameService.getAllPitcherInnings();

    // 규정이닝 이상인 선수들만 필터링
    List<PlayerRankingDto> rankings = new ArrayList<>();

    for (PitcherCalculatedStats stat : allStats) {
      String teamName = stat.getPlayer().getTeamName();
      Double minInnings = teamInningsCriteria.get(teamName);

      // 직접 PitcherStats 조회
      Optional<PitcherStats> pitcherStatsOpt = pitcherStatsRepository.findByPlayerId(stat.getPlayer().getId());

      if (pitcherStatsOpt.isEmpty()) {
        continue;
      }

      PitcherStats pitcherStats = pitcherStatsOpt.get();
      Double playerInnings = pitcherStats.getInningsPitched();

      if (playerInnings == null || playerInnings == 0.0) {
        continue;
      }

      if (minInnings != null && playerInnings >= minInnings) {
        PlayerRankingDto dto = new PlayerRankingDto();
        dto.setPlayerId(stat.getPlayer().getId());
        dto.setPlayerName(stat.getPlayer().getPlayerName());
        dto.setTeamName(teamName);
        dto.setImageUrl(stat.getPlayer().getImageUrl());
        dto.setWhip(stat.getWhip());
        dto.setRankingType("WHIP");
        rankings.add(dto);
      }
    }

    // WHIP 기준으로 정렬 (낮을수록 좋음)
    rankings.sort((a, b) -> Double.compare(a.getWhip(), b.getWhip()));

    // 순위 설정
    for (int i = 0; i < rankings.size(); i++) {
      rankings.get(i).setRank(i + 1);
    }

    return rankings;
  }
}
