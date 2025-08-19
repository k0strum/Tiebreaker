package com.Tiebreaker.controller.kboinfo;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.Tiebreaker.service.kboinfo.KboPlayerService;
import com.Tiebreaker.dto.kboInfo.PlayerDetailResponseDto;

@RestController
@RequestMapping("/api/player")
@RequiredArgsConstructor
@Slf4j
public class kboPlayerController {

  private final KboPlayerService kboPlayerService;

  /**
   * 선수 상세 정보 (기본 + 시즌 + 월별)
   */
  @GetMapping("/{playerId}")
  public ResponseEntity<PlayerDetailResponseDto> getPlayerDetail(@PathVariable Long playerId) {
    return ResponseEntity.ok(kboPlayerService.getPlayerDetail(playerId));
  }
}
