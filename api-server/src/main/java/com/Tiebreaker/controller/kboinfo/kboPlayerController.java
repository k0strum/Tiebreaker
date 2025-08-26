package com.Tiebreaker.controller.kboinfo;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.*;
import com.Tiebreaker.service.kboinfo.KboPlayerService;
import com.Tiebreaker.service.ImageService;
import com.Tiebreaker.dto.kboInfo.PlayerDetailResponseDto;

@RestController
@RequestMapping("/api/player")
@RequiredArgsConstructor
@Slf4j
public class kboPlayerController {

  private final KboPlayerService kboPlayerService;
  private final ImageService imageService;

  /**
   * 선수 상세 정보 (기본 + 시즌 + 월별)
   */
  @GetMapping("/{playerId}")
  public ResponseEntity<PlayerDetailResponseDto> getPlayerDetail(@PathVariable Long playerId) {
    return ResponseEntity.ok(kboPlayerService.getPlayerDetail(playerId));
  }

  /**
   * 선수 프로필 이미지 제공
   */
  @GetMapping("/images/{fileName}")
  public ResponseEntity<Resource> getPlayerImage(@PathVariable String fileName) {
    try {
      Resource resource = imageService.loadPlayerImageAsResource(fileName);
      return ResponseEntity.ok()
          .contentType(MediaType.IMAGE_JPEG)
          .body(resource);
    } catch (Exception e) {
      log.error("선수 이미지 로드 실패: {}", fileName, e);
      // 기본 이미지 반환
      try {
        Resource defaultResource = imageService.loadPlayerImageAsResource("player-default.svg");
        return ResponseEntity.ok()
            .contentType(MediaType.valueOf("image/svg+xml"))
            .body(defaultResource);
      } catch (Exception ex) {
        return ResponseEntity.notFound().build();
      }
    }
  }
}
