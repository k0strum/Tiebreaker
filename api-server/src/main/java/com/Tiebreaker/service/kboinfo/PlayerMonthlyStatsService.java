package com.Tiebreaker.service.kboinfo;

import com.Tiebreaker.dto.kboInfo.BatterMonthlyStatsDto;
import com.Tiebreaker.dto.kboInfo.PitcherMonthlyStatsDto;
import com.Tiebreaker.entity.kboInfo.BatterMonthlyStats;
import com.Tiebreaker.entity.kboInfo.PitcherMonthlyStats;
import com.Tiebreaker.entity.kboInfo.Player;
import com.Tiebreaker.repository.kboInfo.BatterMonthlyStatsRepository;
import com.Tiebreaker.repository.kboInfo.PitcherMonthlyStatsRepository;
import com.Tiebreaker.repository.kboInfo.PlayerRepository;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PlayerMonthlyStatsService {

  private final PlayerRepository playerRepository;
  private final BatterMonthlyStatsRepository batterMonthlyStatsRepository;
  private final PitcherMonthlyStatsRepository pitcherMonthlyStatsRepository;

  @Transactional(readOnly = true)
  public List<BatterMonthlyStatsDto> getBatterMonthlyStats(Long playerId, Integer year) {
    return batterMonthlyStatsRepository.findByPlayer_IdAndYearOrderByMonthAsc(playerId, year)
        .stream()
        .map(this::toBatterDto)
        .collect(Collectors.toList());
  }

  @Transactional(readOnly = true)
  public List<PitcherMonthlyStatsDto> getPitcherMonthlyStats(Long playerId, Integer year) {
    return pitcherMonthlyStatsRepository.findByPlayer_IdAndYearOrderByMonthAsc(playerId, year)
        .stream()
        .map(this::toPitcherDto)
        .collect(Collectors.toList());
  }

  @Transactional
  public void upsertBatterMonthly(Long playerId, BatterMonthlyStatsDto dto) {
    Player player = getPlayer(playerId);
    Optional<BatterMonthlyStats> existing = batterMonthlyStatsRepository
        .findByPlayer_IdAndYearAndMonth(playerId, dto.getYear(), dto.getMonth());

    BatterMonthlyStats entity = existing.orElseGet(BatterMonthlyStats::new);
    entity.setPlayer(player);
    entity.setYear(dto.getYear());
    entity.setMonth(dto.getMonth());
    entity.setGames(dto.getGames());
    entity.setPlateAppearances(dto.getPlateAppearances());
    entity.setAtBats(dto.getAtBats());
    entity.setHits(dto.getHits());
    entity.setDoubles(dto.getDoubles());
    entity.setTriples(dto.getTriples());
    entity.setHomeRuns(dto.getHomeRuns());
    entity.setRunsBattedIn(dto.getRunsBattedIn());
    entity.setRuns(dto.getRuns());
    entity.setWalks(dto.getWalks());
    entity.setHitByPitch(dto.getHitByPitch());
    entity.setStrikeouts(dto.getStrikeouts());
    entity.setStolenBases(dto.getStolenBases());
    entity.setCaughtStealing(dto.getCaughtStealing());
    entity.setGroundedIntoDoublePlay(dto.getGroundedIntoDoublePlay());

    batterMonthlyStatsRepository.save(entity);
  }

  @Transactional
  public void upsertPitcherMonthly(Long playerId, PitcherMonthlyStatsDto dto) {
    Player player = getPlayer(playerId);
    Optional<PitcherMonthlyStats> existing = pitcherMonthlyStatsRepository
        .findByPlayer_IdAndYearAndMonth(playerId, dto.getYear(), dto.getMonth());

    PitcherMonthlyStats entity = existing.orElseGet(PitcherMonthlyStats::new);
    entity.setPlayer(player);
    entity.setYear(dto.getYear());
    entity.setMonth(dto.getMonth());
    entity.setGames(dto.getGames());
    entity.setInningsPitchedInteger(dto.getInningsPitchedInteger());
    entity.setInningsPitchedFraction(dto.getInningsPitchedFraction());
    entity.setStrikeouts(dto.getStrikeouts());
    entity.setRunsAllowed(dto.getRunsAllowed());
    entity.setEarnedRuns(dto.getEarnedRuns());
    entity.setHitsAllowed(dto.getHitsAllowed());
    entity.setHomeRunsAllowed(dto.getHomeRunsAllowed());
    entity.setTotalBattersFaced(dto.getTotalBattersFaced());
    entity.setWalksAllowed(dto.getWalksAllowed());
    entity.setHitByPitch(dto.getHitByPitch());
    entity.setWins(dto.getWins());
    entity.setLosses(dto.getLosses());
    entity.setSaves(dto.getSaves());
    entity.setHolds(dto.getHolds());

    pitcherMonthlyStatsRepository.save(entity);
  }

  private Player getPlayer(Long playerId) {
    return playerRepository.findById(playerId)
        .orElseThrow(() -> new IllegalArgumentException("player not found: " + playerId));
  }

  private BatterMonthlyStatsDto toBatterDto(BatterMonthlyStats e) {
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

  private PitcherMonthlyStatsDto toPitcherDto(PitcherMonthlyStats e) {
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
}
