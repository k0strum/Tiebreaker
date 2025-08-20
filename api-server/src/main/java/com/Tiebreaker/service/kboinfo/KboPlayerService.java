package com.Tiebreaker.service.kboinfo;

import com.Tiebreaker.entity.kboInfo.*;
import com.Tiebreaker.repository.kboInfo.*;
import com.Tiebreaker.dto.kboInfo.PlayerDetailResponseDto;
import com.Tiebreaker.dto.kboInfo.BatterMonthlyStatsDto;
import com.Tiebreaker.dto.kboInfo.PitcherMonthlyStatsDto;
import com.Tiebreaker.dto.kboInfo.KboConstantsDto;
import com.Tiebreaker.constant.PlayerType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.time.Year;

@Service
@RequiredArgsConstructor
@Slf4j
public class KboPlayerService {

  private final PlayerRepository playerRepository;
  private final BatterStatsRepository batterStatsRepository;
  private final PitcherStatsRepository pitcherStatsRepository;
  private final BatterMonthlyStatsRepository batterMonthlyStatsRepository;
  private final PitcherMonthlyStatsRepository pitcherMonthlyStatsRepository;
  private final KboConstantsRepository kboConstantsRepository;

  /**
   * 모든 선수 목록 조회 (기본 정보만)
   */
  @Transactional(readOnly = true)
  public List<Player> getAllPlayers() {
    return playerRepository.findAll();
  }

  /**
   * 선수 상세 정보 조회 (기본 정보 + 시즌 스탯 + 월별 스탯)
   */
  @Transactional(readOnly = true)
  public PlayerDetailResponseDto getPlayerDetail(Long playerId) {
    Integer currentYear = Year.now().getValue();
    KboConstants kboConstants = kboConstantsRepository.findByYear(currentYear)
        .orElseThrow(() -> new RuntimeException("해당 연도의 리그 상수를 찾을 수 없습니다: " + currentYear));

    Player player = playerRepository.findById(playerId)
        .orElseThrow(() -> new RuntimeException("선수를 찾을 수 없습니다: " + playerId));

    PlayerDetailResponseDto detail = new PlayerDetailResponseDto();
    detail.setKboConstants(convertToKboConstantsDto(kboConstants));
    detail.setId(player.getId());
    detail.setPlayerName(player.getPlayerName());
    detail.setTeamName(player.getTeamName());
    detail.setPosition(player.getPosition());
    detail.setBackNumber(player.getBackNumber());
    detail.setBirthday(player.getBirthday());
    detail.setHeightWeight(player.getHeightWeight());
    detail.setDraftRank(player.getDraftRank());
    detail.setCareer(player.getCareer());
    detail.setImageUrl(player.getImageUrl());
    detail.setPlayerType(player.getPlayerType());

    // 시즌 스탯 (타자)
    Optional<BatterStats> batterStatsOpt = batterStatsRepository.findByPlayerIdAndYear(playerId, currentYear);
    batterStatsOpt.ifPresent(bs -> detail.setBatterStats(convertToBatterStatsDto(bs)));

    // 시즌 스탯 (투수)
    Optional<PitcherStats> pitcherStatsOpt = pitcherStatsRepository.findByPlayerIdAndYear(playerId, currentYear);
    pitcherStatsOpt.ifPresent(ps -> detail.setPitcherStats(convertToPitcherStatsDto(ps)));

    // 월별 스탯 (타자)
    List<BatterMonthlyStats> batterMonthly = batterMonthlyStatsRepository
        .findByPlayer_IdAndYearOrderByMonthAsc(playerId, currentYear);
    if (batterMonthly != null && !batterMonthly.isEmpty()) {
      List<BatterMonthlyStatsDto> batterMonthlyDtos = batterMonthly.stream()
          .map(this::convertToBatterMonthlyStatsDto)
          .collect(Collectors.toList());
      detail.setBatterMonthlyStats(batterMonthlyDtos);
    }

    // 월별 스탯 (투수)
    List<PitcherMonthlyStats> pitcherMonthly = pitcherMonthlyStatsRepository
        .findByPlayer_IdAndYearOrderByMonthAsc(playerId, currentYear);
    if (pitcherMonthly != null && !pitcherMonthly.isEmpty()) {
      List<PitcherMonthlyStatsDto> pitcherMonthlyDtos = pitcherMonthly.stream()
          .map(this::convertToPitcherMonthlyStatsDto)
          .collect(Collectors.toList());
      detail.setPitcherMonthlyStats(pitcherMonthlyDtos);
    }

    return detail;
  }

  private KboConstantsDto convertToKboConstantsDto(KboConstants e) {
    KboConstantsDto dto = new KboConstantsDto();
    dto.setYear(e.getYear());
    dto.setWoba(e.getWoba());
    dto.setScale(e.getScale());
    dto.setEbb(e.getEbb());
    dto.setSingles(e.getSingles());
    dto.setDoubles(e.getDoubles());
    dto.setTriples(e.getTriples());
    dto.setHomeRuns(e.getHomeRuns());
    dto.setSb2(e.getSb2());
    dto.setSb3(e.getSb3());
    dto.setCs2(e.getCs2());
    dto.setCs3(e.getCs3());
    dto.setRunsPerEpa(e.getRunsPerEpa());
    dto.setRpw(e.getRpw());
    dto.setCFip(e.getCFip());
    return dto;
  }

  /**
   * 팀별 선수 목록 조회
   */
  @Transactional(readOnly = true)
  public List<Player> getPlayersByTeam(String teamName) {
    return playerRepository.findByTeamName(teamName);
  }

  /**
   * 선수 타입별 조회 (타자/투수/양쪽)
   */
  @Transactional(readOnly = true)
  public List<Player> getPlayersByType(PlayerType playerType) {
    return playerRepository.findByPlayerType(playerType);
  }

  /**
   * 선수 검색 (이름으로)
   */
  @Transactional(readOnly = true)
  public List<Player> searchPlayersByName(String playerName) {
    return playerRepository.findByPlayerNameContainingIgnoreCase(playerName);
  }

  private com.Tiebreaker.dto.kboInfo.BatterStatsDto convertToBatterStatsDto(BatterStats batterStats) {
    com.Tiebreaker.dto.kboInfo.BatterStatsDto dto = new com.Tiebreaker.dto.kboInfo.BatterStatsDto();
    dto.setYear(batterStats.getYear());

    // === 기본 기록 설정 ===
    dto.setGames(batterStats.getGames());
    dto.setPlateAppearances(batterStats.getPlateAppearances());
    dto.setAtBats(batterStats.getAtBats());
    dto.setHits(batterStats.getHits());
    dto.setDoubles(batterStats.getDoubles());
    dto.setTriples(batterStats.getTriples());
    dto.setHomeRuns(batterStats.getHomeRuns());
    dto.setTotalBases(batterStats.getTotalBases());
    dto.setRunsBattedIn(batterStats.getRunsBattedIn());
    dto.setRuns(batterStats.getRuns());
    dto.setWalks(batterStats.getWalks());
    dto.setHitByPitch(batterStats.getHitByPitch());
    dto.setIntentionalWalks(batterStats.getIntentionalWalks());
    dto.setStrikeouts(batterStats.getStrikeouts());
    dto.setGroundedIntoDoublePlay(batterStats.getGroundedIntoDoublePlay());
    dto.setErrors(batterStats.getErrors());
    dto.setStolenBases(batterStats.getStolenBases());
    dto.setCaughtStealing(batterStats.getCaughtStealing());
    dto.setSacrificeHits(batterStats.getSacrificeHits());
    dto.setSacrificeFlies(batterStats.getSacrificeFlies());

    // === 계산된 기록 설정 ===
    dto.setBattingAverage(batterStats.getBattingAverage());
    dto.setSluggingPercentage(batterStats.getSluggingPercentage());
    dto.setOnBasePercentage(batterStats.getOnBasePercentage());
    dto.setStolenBasePercentage(batterStats.getStolenBasePercentage());
    dto.setOps(batterStats.getOps());
    dto.setBattingAverageWithRunnersInScoringPosition(
        batterStats.getBattingAverageWithRunnersInScoringPosition());
    dto.setPinchHitBattingAverage(batterStats.getPinchHitBattingAverage());

    return dto;
  }

  private com.Tiebreaker.dto.kboInfo.PitcherStatsDto convertToPitcherStatsDto(PitcherStats pitcherStats) {
    com.Tiebreaker.dto.kboInfo.PitcherStatsDto dto = new com.Tiebreaker.dto.kboInfo.PitcherStatsDto();
    dto.setYear(pitcherStats.getYear());

    // === 기본 기록 설정 ===
    dto.setGames(pitcherStats.getGames());
    dto.setWins(pitcherStats.getWins());
    dto.setLosses(pitcherStats.getLosses());
    dto.setSaves(pitcherStats.getSaves());
    dto.setBlownSaves(pitcherStats.getBlownSaves());
    dto.setHolds(pitcherStats.getHolds());
    dto.setCompleteGames(pitcherStats.getCompleteGames());
    dto.setShutouts(pitcherStats.getShutouts());
    dto.setTotalBattersFaced(pitcherStats.getTotalBattersFaced());
    dto.setNumberOfPitches(pitcherStats.getNumberOfPitches());

    // 이닝을 정수+분수 형태로 설정
    dto.setInningsPitchedInteger(pitcherStats.getInningsPitchedInteger());
    dto.setInningsPitchedFraction(pitcherStats.getInningsPitchedFraction());

    dto.setHitsAllowed(pitcherStats.getHitsAllowed());
    dto.setDoublesAllowed(pitcherStats.getDoublesAllowed());
    dto.setTriplesAllowed(pitcherStats.getTriplesAllowed());
    dto.setHomeRunsAllowed(pitcherStats.getHomeRunsAllowed());
    dto.setWalksAllowed(pitcherStats.getWalksAllowed());
    dto.setRunsAllowed(pitcherStats.getRunsAllowed());
    dto.setEarnedRuns(pitcherStats.getEarnedRuns());
    dto.setStrikeouts(pitcherStats.getStrikeouts());
    dto.setQualityStarts(pitcherStats.getQualityStarts());
    dto.setIntentionalWalksAllowed(pitcherStats.getIntentionalWalksAllowed());
    dto.setWildPitches(pitcherStats.getWildPitches());
    dto.setBalks(pitcherStats.getBalks());
    dto.setSacrificeHitsAllowed(pitcherStats.getSacrificeHitsAllowed());
    dto.setSacrificeFliesAllowed(pitcherStats.getSacrificeFliesAllowed());

    // === 계산된 기록 설정 ===
    dto.setEarnedRunAverage(pitcherStats.getEarnedRunAverage());
    dto.setWinningPercentage(pitcherStats.getWinningPercentage());
    dto.setWhip(pitcherStats.getWhip());
    dto.setBattingAverageAgainst(pitcherStats.getBattingAverageAgainst());

    return dto;
  }

  private BatterMonthlyStatsDto convertToBatterMonthlyStatsDto(BatterMonthlyStats e) {
    BatterMonthlyStatsDto dto = new BatterMonthlyStatsDto();
    dto.setYear(e.getYear());
    dto.setMonth(e.getMonth());
    dto.setGames(e.getGames());
    dto.setPlateAppearances(e.getPlateAppearances());
    dto.setAtBats(e.getAtBats());
    dto.setHits(e.getHits());
    dto.setDoubles(e.getDoubles());
    dto.setTriples(e.getTriples());
    dto.setHomeRuns(e.getHomeRuns());
    dto.setRunsBattedIn(e.getRunsBattedIn());
    dto.setRuns(e.getRuns());
    dto.setWalks(e.getWalks());
    dto.setHitByPitch(e.getHitByPitch());
    dto.setStrikeouts(e.getStrikeouts());
    dto.setStolenBases(e.getStolenBases());
    dto.setCaughtStealing(e.getCaughtStealing());
    dto.setGroundedIntoDoublePlay(e.getGroundedIntoDoublePlay());
    return dto;
  }

  private PitcherMonthlyStatsDto convertToPitcherMonthlyStatsDto(PitcherMonthlyStats e) {
    PitcherMonthlyStatsDto dto = new PitcherMonthlyStatsDto();
    dto.setYear(e.getYear());
    dto.setMonth(e.getMonth());
    dto.setGames(e.getGames());
    dto.setInningsPitchedInteger(e.getInningsPitchedInteger());
    dto.setInningsPitchedFraction(e.getInningsPitchedFraction());
    dto.setStrikeouts(e.getStrikeouts());
    dto.setRunsAllowed(e.getRunsAllowed());
    dto.setEarnedRuns(e.getEarnedRuns());
    dto.setHitsAllowed(e.getHitsAllowed());
    dto.setHomeRunsAllowed(e.getHomeRunsAllowed());
    dto.setTotalBattersFaced(e.getTotalBattersFaced());
    dto.setWalksAllowed(e.getWalksAllowed());
    dto.setHitByPitch(e.getHitByPitch());
    dto.setWins(e.getWins());
    dto.setLosses(e.getLosses());
    dto.setSaves(e.getSaves());
    dto.setHolds(e.getHolds());
    return dto;
  }

  /**
   * 정수+분수 이닝 값을 문자열로 변환하는 메서드
   * 예: integer=73, fraction=1 -> "73 1/3", integer=45, fraction=2 -> "45 2/3"
   */
  private String convertInningsToString(Integer inningsInteger, Integer inningsFraction) {
    if (inningsInteger == null) {
      inningsInteger = 0;
    }
    if (inningsFraction == null) {
      inningsFraction = 0;
    }

    if (inningsFraction == 0) {
      // 정수 이닝
      return String.valueOf(inningsInteger);
    } else if (inningsFraction == 1) {
      // 1/3 이닝
      return inningsInteger == 0 ? "1/3" : inningsInteger + " 1/3";
    } else if (inningsFraction == 2) {
      // 2/3 이닝
      return inningsInteger == 0 ? "2/3" : inningsInteger + " 2/3";
    } else {
      // 기타 분수 (소수점으로 표시)
      double totalInnings = inningsInteger + (inningsFraction * (1.0 / 3.0));
      return String.valueOf(totalInnings);
    }
  }
}
