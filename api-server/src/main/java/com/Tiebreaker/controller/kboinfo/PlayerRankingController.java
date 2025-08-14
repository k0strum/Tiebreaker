package com.Tiebreaker.controller.kboinfo;

import com.Tiebreaker.dto.kboInfo.PlayerRankingDto;
import com.Tiebreaker.service.kboinfo.PlayerRankingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/rankings")
@RequiredArgsConstructor
public class PlayerRankingController {

  private final PlayerRankingService playerRankingService;

  /**
   * 타율 순위 조회
   */
  @GetMapping("/batting-average")
  public ResponseEntity<List<PlayerRankingDto>> getBattingAverageRanking() {
    try {
      List<PlayerRankingDto> rankings = playerRankingService.getBattingAverageRanking();
      return ResponseEntity.ok(rankings);
    } catch (Exception e) {
      log.error("타율 순위 조회 실패: {}", e.getMessage(), e);
      return ResponseEntity.internalServerError().build();
    }
  }

  /**
   * 홈런 순위 조회
   */
  @GetMapping("/home-runs")
  public ResponseEntity<List<PlayerRankingDto>> getHomeRunRanking() {
    try {
      List<PlayerRankingDto> rankings = playerRankingService.getHomeRunRanking();
      return ResponseEntity.ok(rankings);
    } catch (Exception e) {
      log.error("홈런 순위 조회 실패: {}", e.getMessage(), e);
      return ResponseEntity.internalServerError().build();
    }
  }

  /**
   * 타점 순위 조회
   */
  @GetMapping("/rbi")
  public ResponseEntity<List<PlayerRankingDto>> getRbiRanking() {
    try {
      List<PlayerRankingDto> rankings = playerRankingService.getRbiRanking();
      return ResponseEntity.ok(rankings);
    } catch (Exception e) {
      log.error("타점 순위 조회 실패: {}", e.getMessage(), e);
      return ResponseEntity.internalServerError().build();
    }
  }

  /**
   * 출루율 순위 조회
   */
  @GetMapping("/on-base-percentage")
  public ResponseEntity<List<PlayerRankingDto>> getOnBasePercentageRanking() {
    try {
      List<PlayerRankingDto> rankings = playerRankingService.getOnBasePercentageRanking();
      return ResponseEntity.ok(rankings);
    } catch (Exception e) {
      log.error("출루율 순위 조회 실패: {}", e.getMessage(), e);
      return ResponseEntity.internalServerError().build();
    }
  }

  /**
   * OPS 순위 조회
   */
  @GetMapping("/ops")
  public ResponseEntity<List<PlayerRankingDto>> getOpsRanking() {
    try {
      List<PlayerRankingDto> rankings = playerRankingService.getOpsRanking();
      return ResponseEntity.ok(rankings);
    } catch (Exception e) {
      log.error("OPS 순위 조회 실패: {}", e.getMessage(), e);
      return ResponseEntity.internalServerError().build();
    }
  }

  /**
   * 도루 순위 조회
   */
  @GetMapping("/stolen-bases")
  public ResponseEntity<List<PlayerRankingDto>> getStolenBasesRanking() {
    try {
      List<PlayerRankingDto> rankings = playerRankingService.getStolenBasesRanking();
      return ResponseEntity.ok(rankings);
    } catch (Exception e) {
      log.error("도루 순위 조회 실패: {}", e.getMessage(), e);
      return ResponseEntity.internalServerError().build();
    }
  }

  /**
   * 승수 순위 조회
   */
  @GetMapping("/wins")
  public ResponseEntity<List<PlayerRankingDto>> getWinsRanking() {
    try {
      List<PlayerRankingDto> rankings = playerRankingService.getWinsRanking();
      return ResponseEntity.ok(rankings);
    } catch (Exception e) {
      log.error("승수 순위 조회 실패: {}", e.getMessage(), e);
      return ResponseEntity.internalServerError().build();
    }
  }

  /**
   * 세이브 순위 조회
   */
  @GetMapping("/saves")
  public ResponseEntity<List<PlayerRankingDto>> getSavesRanking() {
    try {
      List<PlayerRankingDto> rankings = playerRankingService.getSavesRanking();
      return ResponseEntity.ok(rankings);
    } catch (Exception e) {
      log.error("세이브 순위 조회 실패: {}", e.getMessage(), e);
      return ResponseEntity.internalServerError().build();
    }
  }

  /**
   * 홀드 순위 조회
   */
  @GetMapping("/holds")
  public ResponseEntity<List<PlayerRankingDto>> getHoldsRanking() {
    try {
      List<PlayerRankingDto> rankings = playerRankingService.getHoldsRanking();
      return ResponseEntity.ok(rankings);
    } catch (Exception e) {
      log.error("홀드 순위 조회 실패: {}", e.getMessage(), e);
      return ResponseEntity.internalServerError().build();
    }
  }

  /**
   * 탈삼진 순위 조회
   */
  @GetMapping("/strikeouts")
  public ResponseEntity<List<PlayerRankingDto>> getStrikeoutsRanking() {
    try {
      List<PlayerRankingDto> rankings = playerRankingService.getStrikeoutsRanking();
      return ResponseEntity.ok(rankings);
    } catch (Exception e) {
      log.error("탈삼진 순위 조회 실패: {}", e.getMessage(), e);
      return ResponseEntity.internalServerError().build();
    }
  }

  /**
   * 방어율 순위 조회
   */
  @GetMapping("/era")
  public ResponseEntity<List<PlayerRankingDto>> getEraRanking() {
    try {
      List<PlayerRankingDto> rankings = playerRankingService.getEraRanking();
      return ResponseEntity.ok(rankings);
    } catch (Exception e) {
      log.error("방어율 순위 조회 실패: {}", e.getMessage(), e);
      return ResponseEntity.internalServerError().build();
    }
  }

  /**
   * WHIP 순위 조회
   */
  @GetMapping("/whip")
  public ResponseEntity<List<PlayerRankingDto>> getWhipRanking() {
    try {
      List<PlayerRankingDto> rankings = playerRankingService.getWhipRanking();
      return ResponseEntity.ok(rankings);
    } catch (Exception e) {
      log.error("WHIP 순위 조회 실패: {}", e.getMessage(), e);
      return ResponseEntity.internalServerError().build();
    }
  }
}
