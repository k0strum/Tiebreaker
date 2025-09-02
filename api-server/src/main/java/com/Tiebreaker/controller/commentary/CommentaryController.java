package com.Tiebreaker.controller.commentary;

import com.Tiebreaker.entity.livegame.Commentary;
import com.Tiebreaker.service.livegame.CommentaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class CommentaryController {

  private final CommentaryService commentaryService;

  @GetMapping("/games/{gameId}/livegame")
  public Page<Commentary> list(
      @PathVariable String gameId,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size) {
    return commentaryService.listRecent(gameId, page, size);
  }

  @GetMapping(value = "/sse/games/{gameId}/livegame", produces = "text/event-stream")
  public SseEmitter sse(@PathVariable String gameId) {
    return commentaryService.subscribe(gameId);
  }

  @DeleteMapping("/games/{gameId}/livegame")
  public void clear(@PathVariable String gameId) {
    commentaryService.clearByGame(gameId);
  }
}
