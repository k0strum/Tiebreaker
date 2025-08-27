package com.Tiebreaker.service.kafka;

import com.Tiebreaker.dto.kboInfo.KboRankDto;
import com.Tiebreaker.dto.kboInfo.PlayerMonthlyStatsMessage;
import com.Tiebreaker.dto.kboInfo.PlayerDto;
import com.Tiebreaker.service.kboinfo.PlayerMonthlyStatsService;
import com.Tiebreaker.service.kboInfoCollecting.daily.RankCollectService;
import com.Tiebreaker.service.kboInfoCollecting.daily.PlayerDataCollectService;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

@Service
@RequiredArgsConstructor
public class DailyRecordConsumer {

  private final ObjectMapper objectMapper;
  private final RankCollectService rankCollectService;
  private final PlayerDataCollectService playerDataCollectService;
  private final PlayerMonthlyStatsService playerMonthlyStatsService;

  @KafkaListener(topics = "kbo-team-rank-data", groupId = "tiebreak-group")
  public void consumeRankData(String message) {
    try {
      KboRankDto kboRankDto = objectMapper.readValue(message, KboRankDto.class);

      // DB 저장을 위해 RankService의 메서드 호출
      rankCollectService.updateRanks(kboRankDto);
    } catch (JsonProcessingException e) {
      System.err.println("Error parsing - Team Rank JSON String: " + e);
    }
  }

  @KafkaListener(topics = "kbo-player-yearly", groupId = "tiebreak-group")
  public void consumePlayerData(String message) {
    try {
      PlayerDto playerDto = objectMapper.readValue(message, PlayerDto.class);

      // null 값들을 기본값으로 설정
      if (playerDto.getBirthday() == null)
        playerDto.setBirthday("정보없음");
      if (playerDto.getHeightWeight() == null)
        playerDto.setHeightWeight("정보없음");
      if (playerDto.getDraftRank() == null)
        playerDto.setDraftRank("정보없음");
      if (playerDto.getPosition() == null)
        playerDto.setPosition("정보없음");
      if (playerDto.getCareer() == null)
        playerDto.setCareer("정보없음");

      // DB 저장을 위해 PlayerDataService의 메서드 호출
      playerDataCollectService.savePlayerData(playerDto);

      System.out.println("✅ 선수 데이터 저장 완료: " + playerDto.getPlayerName() + " (ID: " + playerDto.getId() + ")");
    } catch (JsonProcessingException e) {
      System.err.println("❌ Error parsing - Player JSON String: " + e.getMessage());
    } catch (Exception e) {
      System.err.println("❌ Error saving player data: " + e.getMessage());
    }
  }

  @KafkaListener(topics = "kbo-player-monthly", groupId = "tiebreak-group")
  public void consumePlayerMonthly(String message) {
    try {
      PlayerMonthlyStatsMessage stats = objectMapper.readValue(message, PlayerMonthlyStatsMessage.class);
      if (stats.getPlayerId() == null) {
        return;
      }
      Long playerId = stats.getPlayerId();

      // 선수 존재 여부 확인
      if (!playerDataCollectService.isPlayerExists(playerId)) {
        System.out.println("⚠️ 선수 ID " + playerId + "가 아직 등록되지 않았습니다. 월별 데이터 처리를 건너뜁니다.");
        return;
      }

      if (stats.getBatter() != null) {
        stats.getBatter().forEach(dto -> playerMonthlyStatsService.upsertBatterMonthly(playerId, dto));
      }
      if (stats.getPitcher() != null) {
        stats.getPitcher().forEach(dto -> playerMonthlyStatsService.upsertPitcherMonthly(playerId, dto));
      }

      System.out.println(
          "✅ 월별 데이터 저장 완료: 선수 ID " + playerId + " (타자: " + (stats.getBatter() != null ? stats.getBatter().size() : 0)
              + "개월, 투수: " + (stats.getPitcher() != null ? stats.getPitcher().size() : 0) + "개월)");
    } catch (JsonProcessingException e) {
      System.err.println("❌ Error parsing - PlayerMonthly JSON String: " + e.getMessage());
    } catch (Exception e) {
      System.err.println("❌ Error saving player monthly stats: " + e.getMessage());
    }
  }

}
