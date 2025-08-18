package com.Tiebreaker.service.kboinfo;

import com.Tiebreaker.entity.kboInfo.Player;
import com.Tiebreaker.entity.kboInfo.BatterStats;
import com.Tiebreaker.entity.kboInfo.PitcherStats;
import com.Tiebreaker.repository.kboInfo.PlayerRepository;
import com.Tiebreaker.repository.kboInfo.BatterStatsRepository;
import com.Tiebreaker.repository.kboInfo.PitcherStatsRepository;
import com.Tiebreaker.dto.kboInfo.PlayerDto;
import com.Tiebreaker.constant.PlayerType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class KboPlayerService {

  private final PlayerRepository playerRepository;
  private final BatterStatsRepository batterStatsRepository;
  private final PitcherStatsRepository pitcherStatsRepository;

  /**
   * 모든 선수 목록 조회 (기본 정보만)
   */
  @Transactional(readOnly = true)
  public List<Player> getAllPlayers() {
    return playerRepository.findAll();
  }

  /**
   * 특정 선수의 모든 정보 조회 (기본 정보 + 스탯)
   */
  @Transactional(readOnly = true)
  public PlayerDto getPlayerWithStats(Long playerId) {
    Player player = playerRepository.findById(playerId)
        .orElseThrow(() -> new RuntimeException("선수를 찾을 수 없습니다: " + playerId));

    PlayerDto playerDto = convertToPlayerDto(player);

    // 타자 스탯 조회 (통합된 BatterStats 사용) - 현재 연도 기준
    Integer currentYear = java.time.Year.now().getValue();
    Optional<BatterStats> batterStats = batterStatsRepository.findByPlayerIdAndYear(playerId, currentYear);
    if (batterStats.isPresent()) {
      playerDto.setBatterStats(convertToBatterStatsDto(batterStats.get()));
    }

    // 투수 스탯 조회 (통합된 PitcherStats 사용) - 현재 연도 기준
    Optional<PitcherStats> pitcherStats = pitcherStatsRepository.findByPlayerIdAndYear(playerId, currentYear);
    if (pitcherStats.isPresent()) {
      playerDto.setPitcherStats(convertToPitcherStatsDto(pitcherStats.get()));
    }

    return playerDto;
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

  // Entity to DTO 변환 메서드들
  private PlayerDto convertToPlayerDto(Player player) {
    PlayerDto playerDto = new PlayerDto();
    playerDto.setId(player.getId());
    playerDto.setPlayerName(player.getPlayerName());
    playerDto.setTeamName(player.getTeamName());
    playerDto.setBirthday(player.getBirthday());
    playerDto.setHeightWeight(player.getHeightWeight());
    playerDto.setDraftRank(player.getDraftRank());
    playerDto.setBackNumber(player.getBackNumber());
    playerDto.setPosition(player.getPosition());
    playerDto.setCareer(player.getCareer());
    playerDto.setImageUrl(player.getImageUrl());
    playerDto.setPlayerType(player.getPlayerType());
    return playerDto;
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
