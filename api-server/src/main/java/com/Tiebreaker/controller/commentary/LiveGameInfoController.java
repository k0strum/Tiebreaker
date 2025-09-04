package com.Tiebreaker.controller.commentary;

import com.Tiebreaker.entity.livegame.LiveGameInfo;
import com.Tiebreaker.service.livegame.LiveGameInfoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/live-games")
@RequiredArgsConstructor
public class LiveGameInfoController {

  private final LiveGameInfoService liveGameInfoService;

  /**
   * 실시간 경기 정보 SSE 구독
   */
  @GetMapping(value = "/{gameId}/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
  public SseEmitter subscribe(@PathVariable String gameId) {
    return liveGameInfoService.subscribe(gameId);
  }

  /**
   * 특정 경기의 최신 실시간 정보 조회
   */
  @GetMapping("/{gameId}")
  public ResponseEntity<LiveGameInfo> getLatestByGameId(@PathVariable String gameId) {
    Optional<LiveGameInfo> liveGameInfo = liveGameInfoService.getLatestByGameId(gameId);
    return liveGameInfo.map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  /**
   * 진행 중인 경기들 조회 (오늘 갱신된 것만)
   */
  @GetMapping("/live")
  public ResponseEntity<List<LiveGameInfo>> getLiveGames() {
    List<LiveGameInfo> liveGames = liveGameInfoService.getTodayActiveGames();
    return ResponseEntity.ok(liveGames);
  }

  /**
   * 모든 활성 경기들 조회 (과거 데이터 포함 - 관리자용)
   */
  @GetMapping("/live/all")
  public ResponseEntity<List<LiveGameInfo>> getAllLiveGames() {
    List<LiveGameInfo> liveGames = liveGameInfoService.getLiveGames();
    return ResponseEntity.ok(liveGames);
  }

  /**
   * 실시간 중계방 상태 확인 (gameId 목록으로 조회)
   */
  @PostMapping("/status")
  public ResponseEntity<Map<String, Boolean>> getLiveRoomStatus(@RequestBody List<String> gameIds) {
    Map<String, Boolean> statusMap = new HashMap<>();

    for (String gameId : gameIds) {
      Optional<LiveGameInfo> liveGameInfo = liveGameInfoService.getLatestByGameId(gameId);
      // READY, LIVE 상태인 경우에만 활성화
      boolean isActive = liveGameInfo.isPresent() &&
          (liveGameInfo.get().getStatus().equals("READY") ||
              liveGameInfo.get().getStatus().equals("LIVE"));
      statusMap.put(gameId, isActive);
    }

    return ResponseEntity.ok(statusMap);
  }
}
