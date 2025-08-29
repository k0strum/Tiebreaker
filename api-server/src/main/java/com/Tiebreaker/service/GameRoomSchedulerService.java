package com.Tiebreaker.service;

import com.Tiebreaker.entity.livegame.GameSchedule;
import com.Tiebreaker.repository.GameScheduleRepository;
import com.Tiebreaker.service.livegame.LiveGameInfoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class GameRoomSchedulerService {

  private final GameScheduleRepository gameScheduleRepository;
  private final SimpMessagingTemplate messagingTemplate;
  private final LiveGameInfoService liveGameInfoService;

  @Scheduled(fixedRate = 300000) // 5분마다 실행
  public void manageGameRooms() {
    log.info("게임 룸 스케줄러 실행 시작");

    LocalDateTime now = LocalDateTime.now();
    LocalDateTime thirtyMinutesFromNow = now.plusMinutes(30); // 30분 전
    LocalDateTime oneHourAgo = now.minusHours(1);

    // 1. 30분 이내에 시작할 경기들 - 방 생성 및 시작 알림

    // 디버깅용 로그 추가
    log.info("현재 시간: {}", now);
    log.info("30분 후 시간: {}", thirtyMinutesFromNow);

    // 최적화된 쿼리로 30분 이내 시작할 경기들 조회
    String startTime = now.toString();
    String endTime = thirtyMinutesFromNow.toString();
    List<GameSchedule> upcomingGames = gameScheduleRepository.findUpcomingGamesInRange(startTime, endTime);
    log.info("30분 이내 시작할 경기 수: {}", upcomingGames.size());

    int roomCreatedCount = 0;
    for (GameSchedule game : upcomingGames) {
      try {
        log.info("경기 정보: {} - {} vs {} (시작시간: {})",
            game.getGameId(), game.getHomeTeamName(), game.getAwayTeamName(), game.getGameDateTime());
        createGameRoom(game);
        sendGameStartNotification(game);
        roomCreatedCount++;
      } catch (Exception e) {
        log.error("경기 처리 오류: {} - {}", game.getGameId(), e.getMessage());
      }
    }

    // 2. 1시간 전에 종료된 경기들 - 방 정리 및 종료 알림
    List<GameSchedule> finishedGames = gameScheduleRepository.findByGameDateTimeBeforeAndStatusCode(
        oneHourAgo.toString(), "FINISHED");

    for (GameSchedule game : finishedGames) {
      cleanupGameRoom(game);
      sendGameEndNotification(game);
    }

    log.info("게임 룸 스케줄러 실행 완료 - 방생성: {}개, 방정리: {}개",
        roomCreatedCount, finishedGames.size());
  }

  private void createGameRoom(GameSchedule game) {
    log.info("게임 룸 생성: {} - {}", game.getGameId(),
        game.getHomeTeamName() + " vs " + game.getAwayTeamName());

    // LiveGameInfoService에 초기 방 생성 (READY 상태)
    // 이렇게 하면 사용자가 접속했을 때 바로 방이 존재하게 됨
    liveGameInfoService.createInitialRoom(game.getGameId(), game.getHomeTeamName(), game.getAwayTeamName());
  }

  private void cleanupGameRoom(GameSchedule game) {
    log.info("게임 룸 정리: {} - {}", game.getGameId(),
        game.getHomeTeamName() + " vs " + game.getAwayTeamName());

    // LiveGameInfoService에서 방 정리
    liveGameInfoService.cleanupRoom(game.getGameId());
  }

  private void sendGameStartNotification(GameSchedule game) {
    try {
      log.info("경기 시작 알림 발송: {} - {}", game.getGameId(),
          game.getHomeTeamName() + " vs " + game.getAwayTeamName());

      // WebSocket을 통해 해당 경기 채팅방에 알림 발송
      String destination = "/topic/chat." + game.getGameId();
      String timeStr = LocalDateTime.parse(game.getGameDateTime().replace(" ", "T")).toLocalTime().toString()
          .substring(0, 5);
      String message = String.format("🚨 %s vs %s 경기가 곧 시작됩니다! (%s)",
          game.getHomeTeamName(), game.getAwayTeamName(), timeStr);

      messagingTemplate.convertAndSend(destination, createSystemMessage(message));
    } catch (Exception e) {
      log.error("경기 시작 알림 발송 실패: gameId={}, error={}", game.getGameId(), e.getMessage(), e);
    }
  }

  private void sendGameEndNotification(GameSchedule game) {
    log.info("경기 종료 알림 발송: {} - {}", game.getGameId(),
        game.getHomeTeamName() + " vs " + game.getAwayTeamName());

    // WebSocket을 통해 해당 경기 채팅방에 알림 발송
    String destination = "/topic/chat." + game.getGameId();
    String message = String.format("🏁 %s vs %s 경기가 종료되었습니다.",
        game.getHomeTeamName(), game.getAwayTeamName());

    messagingTemplate.convertAndSend(destination, createSystemMessage(message));
  }

  private Object createSystemMessage(String messageContent) {
    return new Object() {
      public final String sender = "SYSTEM";
      public final String content = messageContent;
      public final long ts = System.currentTimeMillis();
      public final String type = "NOTIFICATION";
    };
  }
}
