package com.Tiebreaker.controller.kboinfo;

import com.Tiebreaker.dto.kboInfo.BatterMonthlyStatsDto;
import com.Tiebreaker.dto.kboInfo.PitcherMonthlyStatsDto;
import com.Tiebreaker.service.kboinfo.PlayerMonthlyStatsService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/players")
@RequiredArgsConstructor
public class PlayerMonthlyStatsController {

  private final PlayerMonthlyStatsService playerMonthlyStatsService;

  @GetMapping("/{playerId}/monthly/batter")
  public ResponseEntity<List<BatterMonthlyStatsDto>> getBatterMonthly(
      @PathVariable Long playerId,
      @RequestParam Integer year) {
    return ResponseEntity.ok(playerMonthlyStatsService.getBatterMonthlyStats(playerId, year));
  }

  @GetMapping("/{playerId}/monthly/pitcher")
  public ResponseEntity<List<PitcherMonthlyStatsDto>> getPitcherMonthly(
      @PathVariable Long playerId,
      @RequestParam Integer year) {
    return ResponseEntity.ok(playerMonthlyStatsService.getPitcherMonthlyStats(playerId, year));
  }
}
