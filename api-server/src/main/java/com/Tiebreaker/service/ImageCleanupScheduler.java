package com.Tiebreaker.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ImageCleanupScheduler {

  private final ImageService imageService;

  /**
   * 매일 새벽 4시에 모든 선수 이미지를 일괄 삭제
   * (긴급 상황이나 디스크 공간 확보가 필요한 경우 사용)
   */
  @Scheduled(cron = "0 0 4 * * *") // 매일 새벽 4시
  public void cleanupPlayerImages() {
    log.info("선수 이미지 일괄 삭제 시작");
    imageService.deleteAllPlayerImages();
    log.info("선수 이미지 일괄 삭제 완료");
  }
}
