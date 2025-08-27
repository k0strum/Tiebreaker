package com.Tiebreaker.controller.kboinfo;

import com.Tiebreaker.entity.GameSchedule;
import com.Tiebreaker.service.GameScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/games")
@RequiredArgsConstructor
public class GameScheduleController {

  private final GameScheduleService service;

  @GetMapping
  public ResponseEntity<List<GameSchedule>> listByDate(@RequestParam String date) {
    return ResponseEntity.ok(service.getByDate(date));
  }

  @GetMapping("/today")
  public ResponseEntity<List<GameSchedule>> today() {
    return ResponseEntity.ok(service.getToday());
  }

  @GetMapping("/{gameId}")
  public ResponseEntity<GameSchedule> getOne(@PathVariable String gameId) {
    GameSchedule found = service.getById(gameId);
    return ResponseEntity.ok(found);
  }
}
