package com.Tiebreaker.service.kboInfoCollecting.daily;

import com.Tiebreaker.constant.PlayerType;
import com.Tiebreaker.dto.kboInfo.PlayerDto;
import com.Tiebreaker.dto.kboInfo.BatterStatsDto;
import com.Tiebreaker.dto.kboInfo.PitcherStatsDto;
import com.Tiebreaker.dto.kboInfo.BatterCalculatedStatsDto;
import com.Tiebreaker.dto.kboInfo.PitcherCalculatedStatsDto;
import com.Tiebreaker.entity.kboInfo.Player;
import com.Tiebreaker.entity.kboInfo.BatterStats;
import com.Tiebreaker.entity.kboInfo.PitcherStats;
import com.Tiebreaker.entity.kboInfo.BatterCalculatedStats;
import com.Tiebreaker.entity.kboInfo.PitcherCalculatedStats;
import com.Tiebreaker.constant.PlayerStatus;
import com.Tiebreaker.repository.kboInfo.PlayerRepository;
import com.Tiebreaker.repository.kboInfo.BatterStatsRepository;
import com.Tiebreaker.repository.kboInfo.PitcherStatsRepository;
import com.Tiebreaker.repository.kboInfo.BatterCalculatedStatsRepository;
import com.Tiebreaker.repository.kboInfo.PitcherCalculatedStatsRepository;
import com.Tiebreaker.service.ImageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PlayerDataCollectService {

  private final PlayerRepository playerRepository;
  private final BatterStatsRepository batterStatsRepository;
  private final BatterCalculatedStatsRepository batterCalculatedStatsRepository;
  private final PitcherStatsRepository pitcherStatsRepository;
  private final PitcherCalculatedStatsRepository pitcherCalculatedStatsRepository;
  private final ImageService imageService;

  @Transactional
  public void savePlayerData(PlayerDto playerDto) {
    // 1. 선수 기본 정보 저장 또는 업데이트
    Player player = saveOrUpdatePlayer(playerDto);

    boolean isBatter = false;
    boolean isPitcher = false;

    // 2. 타자 순수 스탯 저장 (있는 경우)
    if (playerDto.getBatterStats() != null) {
      saveBatterStats(player, playerDto.getBatterStats());
      isBatter = true;
    }

    // 3. 타자 계산 지표 저장 (있는 경우)
    if (playerDto.getBatterCalculatedStats() != null) {
      saveBatterCalculatedStats(player, playerDto.getBatterCalculatedStats());
    }

    // 4. 투수 순수 스탯 저장 (있는 경우)
    if (playerDto.getPitcherStats() != null) {
      savePitcherStats(player, playerDto.getPitcherStats());
      isPitcher = true;
    }

    // 5. 투수 계산 지표 저장 (있는 경우)
    if (playerDto.getPitcherCalculatedStats() != null) {
      savePitcherCalculatedStats(player, playerDto.getPitcherCalculatedStats());
    }

    player.setPlayerType(determinePlayerType(isBatter, isPitcher));

  }

  private Player saveOrUpdatePlayer(PlayerDto playerDto) {
    Player player = playerRepository.findById(playerDto.getId())
        .orElse(new Player());

    // 기본 정보 설정
    player.setId(playerDto.getId());
    player.setPlayerName(playerDto.getPlayerName());
    player.setTeamName(playerDto.getTeamName());
    player.setStatus(PlayerStatus.ACTIVE); // 기본값으로 ACTIVE 설정

    // 추가 정보가 있는 경우 설정 (크롤링에서 수집되는 경우)
    if (playerDto.getBirthday() != null) {
      player.setBirthday(playerDto.getBirthday());
    }
    if (playerDto.getHeightWeight() != null) {
      player.setHeightWeight(playerDto.getHeightWeight());
    }
    if (playerDto.getDraftRank() != null) {
      player.setDraftRank(playerDto.getDraftRank());
    }
    if (playerDto.getBackNumber() != null) {
      player.setBackNumber(playerDto.getBackNumber());
    }
    if (playerDto.getPosition() != null) {
      player.setPosition(playerDto.getPosition());
    }
    if (playerDto.getCareer() != null) {
      player.setCareer(playerDto.getCareer());
    }

    // 이미지 URL 처리
    if (playerDto.getImageUrl() != null && !playerDto.getImageUrl().trim().isEmpty()) {
      try {
        // 기존 이미지가 있다면 삭제
        if (player.getImageUrl() != null && !player.getImageUrl().trim().isEmpty()) {
          imageService.deleteFile(player.getImageUrl(), "player");
        }

        // 새로운 이미지 다운로드 및 저장
        String savedImagePath = imageService.downloadAndSavePlayerImage(playerDto.getImageUrl());
        if (savedImagePath != null) {
          player.setImageUrl(savedImagePath);
          log.info("선수 이미지 저장 완료: {} -> {}", playerDto.getPlayerName(), savedImagePath);
        } else {
          log.warn("선수 이미지 저장 실패: {}", playerDto.getPlayerName());
        }
      } catch (Exception e) {
        log.error("선수 이미지 처리 중 오류 발생: {} - {}", playerDto.getPlayerName(), e.getMessage(), e);
      }
    }

    return playerRepository.save(player);
  }

  private void saveBatterStats(Player player, BatterStatsDto batterStatsDto) {
    BatterStats batterStats = batterStatsRepository.findByPlayerId(player.getId())
        .orElse(new BatterStats());

    batterStats.setPlayer(player);
    batterStats.setGames(batterStatsDto.getGames());
    batterStats.setPlateAppearances(batterStatsDto.getPlateAppearances());
    batterStats.setAtBats(batterStatsDto.getAtBats());
    batterStats.setHits(batterStatsDto.getHits());
    batterStats.setDoubles(batterStatsDto.getDoubles());
    batterStats.setTriples(batterStatsDto.getTriples());
    batterStats.setHomeRuns(batterStatsDto.getHomeRuns());
    batterStats.setTotalBases(batterStatsDto.getTotalBases());
    batterStats.setRunsBattedIn(batterStatsDto.getRunsBattedIn());
    batterStats.setRuns(batterStatsDto.getRuns());
    batterStats.setWalks(batterStatsDto.getWalks());
    batterStats.setHitByPitch(batterStatsDto.getHitByPitch());
    batterStats.setIntentionalWalks(batterStatsDto.getIntentionalWalks());
    batterStats.setStrikeouts(batterStatsDto.getStrikeouts());
    batterStats.setGroundedIntoDoublePlay(batterStatsDto.getGroundedIntoDoublePlay());
    batterStats.setErrors(batterStatsDto.getErrors());
    batterStats.setStolenBases(batterStatsDto.getStolenBases());
    batterStats.setCaughtStealing(batterStatsDto.getCaughtStealing());
    batterStats.setSacrificeHits(batterStatsDto.getSacrificeHits());
    batterStats.setSacrificeFlies(batterStatsDto.getSacrificeFlies());

    batterStatsRepository.save(batterStats);
  }

  private void saveBatterCalculatedStats(Player player, BatterCalculatedStatsDto batterCalculatedStatsDto) {
    BatterCalculatedStats batterCalculatedStats = batterCalculatedStatsRepository.findByPlayerId(player.getId())
        .orElse(new BatterCalculatedStats());

    batterCalculatedStats.setPlayer(player);
    batterCalculatedStats.setBattingAverage(batterCalculatedStatsDto.getBattingAverage());
    batterCalculatedStats.setSluggingPercentage(batterCalculatedStatsDto.getSluggingPercentage());
    batterCalculatedStats.setOnBasePercentage(batterCalculatedStatsDto.getOnBasePercentage());
    batterCalculatedStats.setStolenBasePercentage(batterCalculatedStatsDto.getStolenBasePercentage());
    batterCalculatedStats.setOps(batterCalculatedStatsDto.getOps());
    batterCalculatedStats.setBattingAverageWithRunnersInScoringPosition(
        batterCalculatedStatsDto.getBattingAverageWithRunnersInScoringPosition());
    batterCalculatedStats.setPinchHitBattingAverage(batterCalculatedStatsDto.getPinchHitBattingAverage());

    batterCalculatedStatsRepository.save(batterCalculatedStats);
  }

  private void savePitcherStats(Player player, PitcherStatsDto pitcherStatsDto) {
    PitcherStats pitcherStats = pitcherStatsRepository.findByPlayerId(player.getId())
        .orElse(new PitcherStats());

    pitcherStats.setPlayer(player);
    pitcherStats.setGames(pitcherStatsDto.getGames());
    pitcherStats.setWins(pitcherStatsDto.getWins());
    pitcherStats.setLosses(pitcherStatsDto.getLosses());
    pitcherStats.setSaves(pitcherStatsDto.getSaves());
    pitcherStats.setBlownSaves(pitcherStatsDto.getBlownSaves());
    pitcherStats.setHolds(pitcherStatsDto.getHolds());
    pitcherStats.setCompleteGames(pitcherStatsDto.getCompleteGames());
    pitcherStats.setShutouts(pitcherStatsDto.getShutouts());
    pitcherStats.setTotalBattersFaced(pitcherStatsDto.getTotalBattersFaced());
    pitcherStats.setNumberOfPitches(pitcherStatsDto.getNumberOfPitches());

    // 이닝 파싱 로직 추가
    Double parsedInnings = parseInningsPitched(pitcherStatsDto.getInningsPitched());
    log.info("선수 {} 이닝 파싱 결과: '{}' -> {}", player.getPlayerName(), pitcherStatsDto.getInningsPitched(), parsedInnings);
    pitcherStats.setInningsPitched(parsedInnings);

    pitcherStats.setHitsAllowed(pitcherStatsDto.getHitsAllowed());
    pitcherStats.setDoublesAllowed(pitcherStatsDto.getDoublesAllowed());
    pitcherStats.setTriplesAllowed(pitcherStatsDto.getTriplesAllowed());
    pitcherStats.setHomeRunsAllowed(pitcherStatsDto.getHomeRunsAllowed());
    pitcherStats.setWalksAllowed(pitcherStatsDto.getWalksAllowed());
    pitcherStats.setRunsAllowed(pitcherStatsDto.getRunsAllowed());
    pitcherStats.setEarnedRuns(pitcherStatsDto.getEarnedRuns());
    pitcherStats.setStrikeouts(pitcherStatsDto.getStrikeouts());
    pitcherStats.setQualityStarts(pitcherStatsDto.getQualityStarts());

    // 누락된 필드들 추가
    pitcherStats.setIntentionalWalksAllowed(pitcherStatsDto.getIntentionalWalksAllowed());
    pitcherStats.setWildPitches(pitcherStatsDto.getWildPitches());
    pitcherStats.setBalks(pitcherStatsDto.getBalks());
    pitcherStats.setSacrificeHitsAllowed(pitcherStatsDto.getSacrificeHitsAllowed());
    pitcherStats.setSacrificeFliesAllowed(pitcherStatsDto.getSacrificeFliesAllowed());

    pitcherStatsRepository.save(pitcherStats);
  }

  private void savePitcherCalculatedStats(Player player, PitcherCalculatedStatsDto pitcherCalculatedStatsDto) {
    PitcherCalculatedStats pitcherCalculatedStats = pitcherCalculatedStatsRepository.findByPlayerId(player.getId())
        .orElse(new PitcherCalculatedStats());

    pitcherCalculatedStats.setPlayer(player);
    pitcherCalculatedStats.setEarnedRunAverage(pitcherCalculatedStatsDto.getEarnedRunAverage());
    pitcherCalculatedStats.setWinningPercentage(pitcherCalculatedStatsDto.getWinningPercentage());
    pitcherCalculatedStats.setWhip(pitcherCalculatedStatsDto.getWhip());
    pitcherCalculatedStats.setBattingAverageAgainst(pitcherCalculatedStatsDto.getBattingAverageAgainst());

    pitcherCalculatedStatsRepository.save(pitcherCalculatedStats);
  }

  private PlayerType determinePlayerType(boolean isBatter, boolean isPitcher) {
    if (isBatter && isPitcher) {
      return PlayerType.BOTH;
    } else if (isBatter) {
      return PlayerType.BATTER;
    } else if (isPitcher) {
      return PlayerType.PITCHER;
    }
    return PlayerType.NONE;
  }

  /**
   * KBO 이닝 문자열을 Double로 파싱하는 메서드
   * 예: "73 1/3" -> 73.333..., "45 2/3" -> 45.666..., "120" -> 120.0
   */
  private Double parseInningsPitched(String inningsValue) {
    log.info("이닝 파싱 시작 - 원본 값: '{}'", inningsValue);

    if (inningsValue == null || inningsValue.trim().isEmpty()) {
      log.warn("이닝 파싱 - null 또는 빈 값으로 0.0 반환");
      return 0.0;
    }

    String inningsStr = inningsValue.trim();
    log.info("이닝 파싱 - 문자열 변환: '{}'", inningsStr);

    try {
      // "42 1/3" 형태 파싱 (공백으로 구분된 형태)
      if (inningsStr.contains(" ")) {
        String[] parts = inningsStr.split("\\s+"); // 하나 이상의 공백으로 분할
        log.info("이닝 파싱 - 공백 포함 형태 파싱: parts = {}", java.util.Arrays.toString(parts));

        if (parts.length == 2) {
          int wholeInnings = Integer.parseInt(parts[0].trim());
          String fraction = parts[1].trim();
          log.info("이닝 파싱 - 정수 부분: {}, 분수 부분: '{}'", wholeInnings, fraction);

          double fractionValue = 0.0;
          if (fraction.equals("1/3")) {
            fractionValue = 1.0 / 3.0;
          } else if (fraction.equals("2/3")) {
            fractionValue = 2.0 / 3.0;
          } else {
            log.warn("이닝 파싱 - 알 수 없는 분수 형태: {}", fraction);
            return (double) wholeInnings; // 분수 부분을 무시하고 정수만 반환
          }

          double result = wholeInnings + fractionValue;
          log.info("이닝 파싱 - 공백 포함 형태 결과: {} + {} = {}", wholeInnings, fractionValue, result);
          return result;
        } else {
          log.warn("이닝 파싱 - 공백 포함 형태이지만 예상과 다른 구조: parts.length = {}", parts.length);
        }
      }

      // "1/3", "2/3" 형태 파싱 (분수만 있는 경우)
      if (inningsStr.contains("/")) {
        log.info("이닝 파싱 - 분수 형태 파싱: {}", inningsStr);
        if (inningsStr.equals("1/3")) {
          log.info("이닝 파싱 - 1/3 형태 결과: {}", 1.0 / 3.0);
          return 1.0 / 3.0;
        } else if (inningsStr.equals("2/3")) {
          log.info("이닝 파싱 - 2/3 형태 결과: {}", 2.0 / 3.0);
          return 2.0 / 3.0;
        } else {
          log.warn("이닝 파싱 - 알 수 없는 분수 형태: {}", inningsStr);
        }
      }

      // 일반 숫자 형태 (정수 또는 소수)
      double result = Double.parseDouble(inningsStr);
      log.info("이닝 파싱 - 일반 숫자 형태 결과: {}", result);
      return result;

    } catch (NumberFormatException e) {
      log.error("이닝 파싱 실패: {} - {}", inningsStr, e.getMessage());
      return 0.0;
    } catch (Exception e) {
      log.error("이닝 파싱 중 예상치 못한 오류: {} - {}", inningsStr, e.getMessage());
      return 0.0;
    }
  }
}
