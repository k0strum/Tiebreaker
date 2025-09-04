package com.Tiebreaker.service.livegame;

import com.Tiebreaker.dto.livegame.LiveGameInfoDto;
import com.Tiebreaker.entity.livegame.LiveGameInfo;
import com.Tiebreaker.repository.LiveGameInfoRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
@RequiredArgsConstructor
public class LiveGameInfoService {

  private final LiveGameInfoRepository liveGameInfoRepository;
  private final ObjectMapper objectMapper;

  // gameId -> emitters
  private final Map<String, Set<SseEmitter>> emitterByGame = new ConcurrentHashMap<>();

  public LiveGameInfo save(LiveGameInfoDto event) {
    LiveGameInfo liveGameInfo = LiveGameInfo.builder()
        .gameId(event.getGameId())
        .status(event.getStatus())
        .inning(event.getInning())
        .half(event.getHalf())
        .homeScore(event.getHomeScore())
        .awayScore(event.getAwayScore())
        .homeTeam(event.getHomeTeam())
        .awayTeam(event.getAwayTeam())
        .currentBatter(event.getCurrentBatter())
        .currentPitcher(event.getCurrentPitcher())
        .bases(convertBasesToString(event.getBases()))
        .outs(event.getOuts())
        .balls(event.getBalls())
        .strikes(event.getStrikes())
        .timestamp(event.getTimestamp())
        .build();

    LiveGameInfo saved = liveGameInfoRepository.save(liveGameInfo);
    System.out.println("✅ 실시간 경기 정보 저장 완료: " + event.getGameId() + " - " + event.getStatus());
    return saved;
  }

  public Optional<LiveGameInfo> getLatestByGameId(String gameId) {
    return liveGameInfoRepository.findTop1ByGameIdOrderByTimestampDesc(gameId);
  }

  public List<LiveGameInfo> getLiveGames() {
    return liveGameInfoRepository.findByStatusIn(Arrays.asList("READY", "LIVE"));
  }

  /**
   * 오늘 생성되거나 갱신된 활성 경기들만 조회
   * 서버 재시작 후 과거 데이터가 READY 상태로 남는 문제 해결
   */
  public List<LiveGameInfo> getTodayActiveGames() {
    return liveGameInfoRepository.findTodayActiveGames(LocalDate.now());
  }

  /**
   * 과거 데이터 정리 - 어제 이전의 READY 상태 데이터를 FINISHED로 변경
   * 서버 재시작 시 호출하여 과거 데이터 정리
   */
  public void cleanupOldReadyGames() {
    try {
      LocalDate yesterday = LocalDate.now().minusDays(1);

      // 어제 이전의 READY 상태 데이터 조회
      List<LiveGameInfo> oldReadyGames = liveGameInfoRepository.findOldReadyGames(yesterday);

      if (!oldReadyGames.isEmpty()) {
        log.info("과거 READY 상태 데이터 {}개를 FINISHED로 변경합니다.", oldReadyGames.size());

        for (LiveGameInfo game : oldReadyGames) {
          game.setStatus("FINISHED");
          game.setTimestamp(System.currentTimeMillis());
          liveGameInfoRepository.save(game);
          log.info("과거 데이터 정리: {} - {} vs {} (상태: READY → FINISHED)",
              game.getGameId(), game.getHomeTeam(), game.getAwayTeam());
        }

        log.info("과거 데이터 정리 완료: {}개 처리됨", oldReadyGames.size());
      } else {
        log.info("정리할 과거 READY 데이터가 없습니다.");
      }
    } catch (Exception e) {
      log.error("과거 데이터 정리 중 오류 발생: {}", e.getMessage(), e);
    }
  }

  /**
   * 경기 스케줄에 따라 초기 방을 생성합니다 (READY 상태)
   */
  public void createInitialRoom(String gameId, String homeTeam, String awayTeam) {
    try {
      // 입력값 검증
      if (gameId == null || gameId.trim().isEmpty()) {
        log.error("gameId가 null이거나 비어있습니다.");
        return;
      }
      if (homeTeam == null || awayTeam == null) {
        log.error("팀명이 null입니다. gameId: {}, homeTeam: {}, awayTeam: {}", gameId, homeTeam, awayTeam);
        return;
      }

      // 이미 방이 존재하는지 확인
      Optional<LiveGameInfo> existing = getLatestByGameId(gameId);
      if (existing.isPresent()) {
        LiveGameInfo existingRoom = existing.get();
        // READY 또는 LIVE 상태인 경우만 중복 생성 방지
        if ("READY".equals(existingRoom.getStatus()) || "LIVE".equals(existingRoom.getStatus())) {
          log.info("방이 이미 존재합니다: {} (상태: {})", gameId, existingRoom.getStatus());
          return;
        }
      }

      // 초기 방 생성 (READY 상태)
      LiveGameInfo initialRoom = LiveGameInfo.builder()
          .gameId(gameId)
          .status("READY")
          .inning(0)
          .half("T")
          .homeScore(0)
          .awayScore(0)
          .homeTeam(homeTeam)
          .awayTeam(awayTeam)
          .currentBatter("")
          .currentPitcher("")
          .bases("[false,false,false]")
          .outs(0)
          .balls(0)
          .strikes(0)
          .timestamp(System.currentTimeMillis())
          .build();

      liveGameInfoRepository.save(initialRoom);
      log.info("초기 방 생성 완료: {} - {} vs {}", gameId, homeTeam, awayTeam);
    } catch (Exception e) {
      log.error("방 생성 중 오류 발생: gameId={}, homeTeam={}, awayTeam={}, error={}",
          gameId, homeTeam, awayTeam, e.getMessage(), e);
    }
  }

  /**
   * 경기 종료 후 방을 정리합니다
   */
  public void cleanupRoom(String gameId) {
    // SSE 연결된 사용자들에게 방 종료 알림
    Set<SseEmitter> emitters = emitterByGame.getOrDefault(gameId, Collections.emptySet());
    List<SseEmitter> toRemove = new ArrayList<>();

    for (SseEmitter emitter : emitters) {
      try {
        Map<String, Object> data = new HashMap<>();
        data.put("gameId", gameId);
        data.put("status", "FINISHED");
        data.put("message", "경기가 종료되었습니다.");

        emitter.send(SseEmitter.event()
            .name("room-closed")
            .data(data));
      } catch (IOException | IllegalStateException e) {
        toRemove.add(emitter);
      }
    }

    toRemove.forEach(em -> removeEmitter(gameId, em));

    // 방 정보 삭제 (선택사항 - 로그 보존을 위해 주석 처리)
    // liveGameInfoRepository.deleteByGameId(gameId);

    log.info("방 정리 완료: {}", gameId);
  }

  public SseEmitter subscribe(String gameId) {
    SseEmitter emitter = new SseEmitter(0L); // no timeout; rely on proxy timeouts
    emitterByGame.computeIfAbsent(gameId, k -> Collections.synchronizedSet(new HashSet<>()))
        .add(emitter);

    emitter.onCompletion(() -> removeEmitter(gameId, emitter));
    emitter.onTimeout(() -> removeEmitter(gameId, emitter));
    emitter.onError(e -> removeEmitter(gameId, emitter));

    try {
      emitter.send(SseEmitter.event().name("init").data("ok"));
    } catch (IOException ignored) {
    }

    return emitter;
  }

  public void broadcast(LiveGameInfo liveGameInfo) {
    Set<SseEmitter> emitters = emitterByGame.getOrDefault(liveGameInfo.getGameId(), Collections.emptySet());
    List<SseEmitter> toRemove = new ArrayList<>();

    for (SseEmitter emitter : emitters) {
      try {
        Map<String, Object> data = new HashMap<>();
        data.put("gameId", liveGameInfo.getGameId());
        data.put("status", liveGameInfo.getStatus());
        data.put("inning", liveGameInfo.getInning());
        data.put("half", liveGameInfo.getHalf());
        data.put("homeScore", liveGameInfo.getHomeScore());
        data.put("awayScore", liveGameInfo.getAwayScore());
        data.put("homeTeam", liveGameInfo.getHomeTeam());
        data.put("awayTeam", liveGameInfo.getAwayTeam());
        data.put("currentBatter", liveGameInfo.getCurrentBatter());
        data.put("currentPitcher", liveGameInfo.getCurrentPitcher());
        data.put("bases", convertStringToBases(liveGameInfo.getBases()));
        data.put("outs", liveGameInfo.getOuts());
        data.put("balls", liveGameInfo.getBalls());
        data.put("strikes", liveGameInfo.getStrikes());
        data.put("timestamp", liveGameInfo.getTimestamp());

        emitter.send(SseEmitter.event()
            .name("live-game-info")
            .data(data));
      } catch (IOException | IllegalStateException e) { // 두 예외를 한번에 처리
        // INFO 레벨로 간단한 메시지만 출력
        log.info("🔌 SSE client disconnected for gameId: {}. Emitter will be removed.", liveGameInfo.getGameId());
        toRemove.add(emitter);
      }
    }

    toRemove.forEach(em -> removeEmitter(liveGameInfo.getGameId(), em));

    if (!emitters.isEmpty()) {
      System.out.println("✅ 실시간 경기 정보 브로드캐스트 완료: " + liveGameInfo.getGameId() + " (구독자: " + emitters.size() + "명)");
    }
  }

  private void removeEmitter(String gameId, SseEmitter emitter) {
    Set<SseEmitter> set = emitterByGame.get(gameId);
    if (set != null) {
      set.remove(emitter);
      if (set.isEmpty()) {
        emitterByGame.remove(gameId);
      }
    }
  }

  /**
   * Boolean 배열을 JSON 문자열로 변환
   */
  private String convertBasesToString(Boolean[] bases) {
    if (bases == null) {
      return "[false,false,false]";
    }
    try {
      return objectMapper.writeValueAsString(bases);
    } catch (JsonProcessingException e) {
      return "[false,false,false]";
    }
  }

  /**
   * JSON 문자열을 Boolean 배열로 변환
   */
  private Boolean[] convertStringToBases(String basesString) {
    if (basesString == null || basesString.isEmpty()) {
      return new Boolean[] { false, false, false };
    }
    try {
      return objectMapper.readValue(basesString, Boolean[].class);
    } catch (JsonProcessingException e) {
      return new Boolean[] { false, false, false };
    }
  }
}
