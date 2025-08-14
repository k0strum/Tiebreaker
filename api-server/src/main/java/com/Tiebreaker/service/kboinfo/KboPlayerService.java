package com.Tiebreaker.service.kboinfo;

import com.Tiebreaker.entity.kboInfo.Player;
import com.Tiebreaker.entity.kboInfo.BatterStats;
import com.Tiebreaker.entity.kboInfo.PitcherStats;
import com.Tiebreaker.entity.kboInfo.BatterCalculatedStats;
import com.Tiebreaker.entity.kboInfo.PitcherCalculatedStats;
import com.Tiebreaker.repository.kboInfo.PlayerRepository;
import com.Tiebreaker.repository.kboInfo.BatterStatsRepository;
import com.Tiebreaker.repository.kboInfo.PitcherStatsRepository;
import com.Tiebreaker.repository.kboInfo.BatterCalculatedStatsRepository;
import com.Tiebreaker.repository.kboInfo.PitcherCalculatedStatsRepository;
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
  private final BatterCalculatedStatsRepository batterCalculatedStatsRepository;
  private final PitcherCalculatedStatsRepository pitcherCalculatedStatsRepository;

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

    // 타자 스탯 조회
    Optional<BatterStats> batterStats = batterStatsRepository.findByPlayerId(playerId);
    if (batterStats.isPresent()) {
      playerDto.setBatterStats(convertToBatterStatsDto(batterStats.get()));
    }

    // 타자 계산 지표 조회
    Optional<BatterCalculatedStats> batterCalculatedStats = batterCalculatedStatsRepository.findByPlayerId(playerId);
    if (batterCalculatedStats.isPresent()) {
      playerDto.setBatterCalculatedStats(convertToBatterCalculatedStatsDto(batterCalculatedStats.get()));
    }

    // 투수 스탯 조회
    Optional<PitcherStats> pitcherStats = pitcherStatsRepository.findByPlayerId(playerId);
    if (pitcherStats.isPresent()) {
      playerDto.setPitcherStats(convertToPitcherStatsDto(pitcherStats.get()));
    }

    // 투수 계산 지표 조회
    Optional<PitcherCalculatedStats> pitcherCalculatedStats = pitcherCalculatedStatsRepository.findByPlayerId(playerId);
    if (pitcherCalculatedStats.isPresent()) {
      playerDto.setPitcherCalculatedStats(convertToPitcherCalculatedStatsDto(pitcherCalculatedStats.get()));
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
    return dto;
  }

  private com.Tiebreaker.dto.kboInfo.BatterCalculatedStatsDto convertToBatterCalculatedStatsDto(
      BatterCalculatedStats batterCalculatedStats) {
    com.Tiebreaker.dto.kboInfo.BatterCalculatedStatsDto dto = new com.Tiebreaker.dto.kboInfo.BatterCalculatedStatsDto();
    dto.setBattingAverage(batterCalculatedStats.getBattingAverage());
    dto.setSluggingPercentage(batterCalculatedStats.getSluggingPercentage());
    dto.setOnBasePercentage(batterCalculatedStats.getOnBasePercentage());
    dto.setStolenBasePercentage(batterCalculatedStats.getStolenBasePercentage());
    dto.setOps(batterCalculatedStats.getOps());
    dto.setBattingAverageWithRunnersInScoringPosition(
        batterCalculatedStats.getBattingAverageWithRunnersInScoringPosition());
    dto.setPinchHitBattingAverage(batterCalculatedStats.getPinchHitBattingAverage());
    return dto;
  }

  private com.Tiebreaker.dto.kboInfo.PitcherStatsDto convertToPitcherStatsDto(PitcherStats pitcherStats) {
    com.Tiebreaker.dto.kboInfo.PitcherStatsDto dto = new com.Tiebreaker.dto.kboInfo.PitcherStatsDto();
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

    // Double을 다시 원래 형태의 문자열로 변환
    dto.setInningsPitched(convertInningsToString(pitcherStats.getInningsPitched()));

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
    return dto;
  }

  private com.Tiebreaker.dto.kboInfo.PitcherCalculatedStatsDto convertToPitcherCalculatedStatsDto(
      PitcherCalculatedStats pitcherCalculatedStats) {
    com.Tiebreaker.dto.kboInfo.PitcherCalculatedStatsDto dto = new com.Tiebreaker.dto.kboInfo.PitcherCalculatedStatsDto();
    dto.setEarnedRunAverage(pitcherCalculatedStats.getEarnedRunAverage());
    dto.setWinningPercentage(pitcherCalculatedStats.getWinningPercentage());
    dto.setWhip(pitcherCalculatedStats.getWhip());
    dto.setBattingAverageAgainst(pitcherCalculatedStats.getBattingAverageAgainst());
    return dto;
  }

  /**
   * Double 이닝 값을 원래 형태의 문자열로 변환하는 메서드
   * 예: 73.333... -> "73 1/3", 45.666... -> "45 2/3", 120.0 -> "120"
   */
  private String convertInningsToString(Double innings) {
    if (innings == null) {
      return "0";
    }

    int wholeInnings = (int) Math.floor(innings);
    double fraction = innings - wholeInnings;

    // 소수점 오차를 고려한 비교
    if (Math.abs(fraction) < 0.01) {
      // 정수 이닝
      return String.valueOf(wholeInnings);
    } else if (Math.abs(fraction - 1.0 / 3.0) < 0.01) {
      // 1/3 이닝
      return wholeInnings == 0 ? "1/3" : wholeInnings + " 1/3";
    } else if (Math.abs(fraction - 2.0 / 3.0) < 0.01) {
      // 2/3 이닝
      return wholeInnings == 0 ? "2/3" : wholeInnings + " 2/3";
    } else {
      // 기타 소수점 이닝 (소수점으로 표시)
      return String.valueOf(innings);
    }
  }
}
