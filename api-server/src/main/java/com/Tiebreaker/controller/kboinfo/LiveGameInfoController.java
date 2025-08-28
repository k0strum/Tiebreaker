package com.Tiebreaker.controller.kboinfo;

import com.Tiebreaker.entity.LiveGameInfo;
import com.Tiebreaker.service.LiveGameInfoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
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
   * 진행 중인 경기들 조회
   */
  @GetMapping("/live")
  public ResponseEntity<List<LiveGameInfo>> getLiveGames() {
    List<LiveGameInfo> liveGames = liveGameInfoService.getLiveGames();
    return ResponseEntity.ok(liveGames);
  }
}
