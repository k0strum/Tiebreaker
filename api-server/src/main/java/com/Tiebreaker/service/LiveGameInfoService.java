package com.Tiebreaker.service;

import com.Tiebreaker.dto.kboInfo.LiveGameInfoDto;
import com.Tiebreaker.entity.LiveGameInfo;
import com.Tiebreaker.repository.LiveGameInfoRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
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
    return liveGameInfoRepository.findByGameIdOrderByRegDateDesc(gameId);
  }

  public List<LiveGameInfo> getLiveGames() {
    return liveGameInfoRepository.findByStatusIn(Arrays.asList("READY", "LIVE"));
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
