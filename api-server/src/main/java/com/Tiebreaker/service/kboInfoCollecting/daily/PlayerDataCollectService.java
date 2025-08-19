package com.Tiebreaker.service.kboInfoCollecting.daily;

import com.Tiebreaker.constant.PlayerType;
import com.Tiebreaker.dto.kboInfo.PlayerDto;
import com.Tiebreaker.dto.kboInfo.BatterStatsDto;
import com.Tiebreaker.dto.kboInfo.PitcherStatsDto;

import com.Tiebreaker.entity.kboInfo.Player;
import com.Tiebreaker.entity.kboInfo.BatterStats;
import com.Tiebreaker.entity.kboInfo.PitcherStats;
import com.Tiebreaker.constant.PlayerStatus;
import com.Tiebreaker.repository.kboInfo.PlayerRepository;
import com.Tiebreaker.repository.kboInfo.BatterStatsRepository;
import com.Tiebreaker.repository.kboInfo.PitcherStatsRepository;
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
  private final PitcherStatsRepository pitcherStatsRepository;
  private final ImageService imageService;

  @Transactional
  public void savePlayerData(PlayerDto playerDto) {
    log.info("선수 저장 시작: {} ({})", playerDto.getPlayerName(), playerDto.getTeamName());

    // 1. 선수 기본 정보 저장 또는 업데이트
    Player player = saveOrUpdatePlayer(playerDto);

    boolean isBatter = false;
    boolean isPitcher = false;

    // 2. 타자 스탯 저장 (있는 경우)
    if (playerDto.getBatterStats() != null) {
      saveBatterStats(player, playerDto.getBatterStats());
      isBatter = true;
    }

    // 3. 투수 스탯 저장 (있는 경우)
    if (playerDto.getPitcherStats() != null) {
      savePitcherStats(player, playerDto.getPitcherStats());
      isPitcher = true;
    }

    player.setPlayerType(determinePlayerType(isBatter, isPitcher));

    log.info("선수 저장 완료: {} - 타자: {}, 투수: {}",
        playerDto.getPlayerName(), isBatter, isPitcher);
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

    // 이미지 URL 처리 (옵션 A: URL 변경 시에만 업데이트)
    if (playerDto.getImageUrl() != null && !playerDto.getImageUrl().trim().isEmpty()) {
      try {
        String incomingUrl = playerDto.getImageUrl().trim();
        String currentSourceUrl = player.getSourceImageUrl();

        if (currentSourceUrl != null && currentSourceUrl.equals(incomingUrl)) {
          // 변경 없음 → 스킵
          // 로그는 과다출력을 피하기 위해 debug로만 남김
          log.debug("선수 이미지 URL 변경 없음: {}", playerDto.getPlayerName());
        } else {
          // 새 이미지 우선 다운로드
          String previousImagePath = player.getImageUrl();
          String savedImagePath = imageService.downloadAndSavePlayerImage(incomingUrl);
          if (savedImagePath != null) {
            // 성공 시 DB 경로 및 소스 URL 교체
            player.setImageUrl(savedImagePath);
            player.setSourceImageUrl(incomingUrl);
            log.info("선수 이미지 업데이트: {}", playerDto.getPlayerName());

            // 과거 파일 삭제는 성공 후 수행
            if (previousImagePath != null && !previousImagePath.trim().isEmpty()) {
              try {
                imageService.deleteFile(previousImagePath, "player");
              } catch (Exception deleteException) {
                log.warn("기존 이미지 삭제 실패: {} - {}", playerDto.getPlayerName(), deleteException.getMessage());
              }
            }
          } else {
            log.warn("선수 이미지 다운로드 실패: {}", playerDto.getPlayerName());
          }
        }
      } catch (Exception e) {
        log.error("선수 이미지 처리 중 오류 발생: {} - {}", playerDto.getPlayerName(), e.getMessage());
      }
    }

    return playerRepository.save(player);
  }

  private void saveBatterStats(Player player, BatterStatsDto batterStatsDto) {
    // 연도 설정 (기본값: 현재 연도)
    Integer year = batterStatsDto.getYear();
    if (year == null) {
      year = java.time.Year.now().getValue();
    }

    BatterStats batterStats = batterStatsRepository.findByPlayerIdAndYear(player.getId(), year)
        .orElse(new BatterStats());

    batterStats.setPlayer(player);
    batterStats.setYear(year);

    // === 기본 기록 설정 ===
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

    // === 계산된 기록 설정 ===
    batterStats.setBattingAverage(batterStatsDto.getBattingAverage());
    batterStats.setSluggingPercentage(batterStatsDto.getSluggingPercentage());
    batterStats.setOnBasePercentage(batterStatsDto.getOnBasePercentage());
    batterStats.setStolenBasePercentage(batterStatsDto.getStolenBasePercentage());
    batterStats.setOps(batterStatsDto.getOps());
    batterStats
        .setBattingAverageWithRunnersInScoringPosition(batterStatsDto.getBattingAverageWithRunnersInScoringPosition());
    batterStats.setPinchHitBattingAverage(batterStatsDto.getPinchHitBattingAverage());

    batterStatsRepository.save(batterStats);
  }

  private void savePitcherStats(Player player, PitcherStatsDto pitcherStatsDto) {
    // 연도 설정 (기본값: 현재 연도)
    Integer year = pitcherStatsDto.getYear();
    if (year == null) {
      year = java.time.Year.now().getValue();
    }

    PitcherStats pitcherStats = pitcherStatsRepository.findByPlayerIdAndYear(player.getId(), year)
        .orElse(new PitcherStats());

    pitcherStats.setPlayer(player);
    pitcherStats.setYear(year);
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

    // 이닝 설정 - 문자열에서 파싱하여 정수+분수 형태로 변경
    String inningsString = pitcherStatsDto.getInningsPitched();
    if (inningsString != null && !inningsString.trim().isEmpty()) {
      parseAndSetInningsPitched(inningsString, pitcherStats);
    } else {
      // DTO에서 정수+분수 형태로 직접 설정 (백업)
      Integer integerPart = pitcherStatsDto.getInningsPitchedInteger();
      Integer fractionPart = pitcherStatsDto.getInningsPitchedFraction();
      pitcherStats.setInningsPitchedInteger(integerPart != null ? integerPart : 0);
      pitcherStats.setInningsPitchedFraction(fractionPart != null ? fractionPart : 0);
    }

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

    // === 계산된 기록 설정 ===
    pitcherStats.setEarnedRunAverage(pitcherStatsDto.getEarnedRunAverage());
    pitcherStats.setWinningPercentage(pitcherStatsDto.getWinningPercentage());
    pitcherStats.setWhip(pitcherStatsDto.getWhip());
    pitcherStats.setBattingAverageAgainst(pitcherStatsDto.getBattingAverageAgainst());

    pitcherStatsRepository.save(pitcherStats);
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
   * 선수 존재 여부를 확인하는 메서드
   */
  @Transactional(readOnly = true)
  public boolean isPlayerExists(Long playerId) {
    return playerRepository.existsById(playerId);
  }

  /**
   * KBO 이닝 문자열을 정수+분수 형태로 파싱하는 메서드
   * 예: "73 1/3" -> integer: 73, fraction: 1
   */
  private void parseAndSetInningsPitched(String inningsValue, PitcherStats pitcherStats) {
    if (inningsValue == null || inningsValue.trim().isEmpty()) {
      pitcherStats.setInningsPitchedInteger(0);
      pitcherStats.setInningsPitchedFraction(0);
      return;
    }

    String inningsStr = inningsValue.trim();

    try {
      // "42 1/3" 형태 파싱 (공백으로 구분된 형태)
      if (inningsStr.contains(" ")) {
        String[] parts = inningsStr.split("\\s+");

        if (parts.length == 2) {
          int wholeInnings = Integer.parseInt(parts[0].trim());
          String fraction = parts[1].trim();

          int fractionValue = 0;
          if (fraction.equals("1/3")) {
            fractionValue = 1;
          } else if (fraction.equals("2/3")) {
            fractionValue = 2;
          }

          pitcherStats.setInningsPitchedInteger(wholeInnings);
          pitcherStats.setInningsPitchedFraction(fractionValue);
          return;
        }
      }

      // "1/3", "2/3" 형태 파싱 (분수만 있는 경우)
      if (inningsStr.contains("/")) {
        if (inningsStr.equals("1/3")) {
          pitcherStats.setInningsPitchedInteger(0);
          pitcherStats.setInningsPitchedFraction(1);
          return;
        } else if (inningsStr.equals("2/3")) {
          pitcherStats.setInningsPitchedInteger(0);
          pitcherStats.setInningsPitchedFraction(2);
          return;
        }
      }

      // 일반 숫자 형태 (정수)
      int wholeInnings = Integer.parseInt(inningsStr);
      pitcherStats.setInningsPitchedInteger(wholeInnings);
      pitcherStats.setInningsPitchedFraction(0);

    } catch (NumberFormatException e) {
      log.error("이닝 파싱 실패: {} - {}", inningsStr, e.getMessage());
      pitcherStats.setInningsPitchedInteger(0);
      pitcherStats.setInningsPitchedFraction(0);
    } catch (Exception e) {
      log.error("이닝 파싱 중 예상치 못한 오류: {} - {}", inningsStr, e.getMessage());
      pitcherStats.setInningsPitchedInteger(0);
      pitcherStats.setInningsPitchedFraction(0);
    }
  }
}
