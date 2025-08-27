package com.Tiebreaker.service;

import com.Tiebreaker.entity.GameSchedule;
import com.Tiebreaker.repository.GameScheduleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GameScheduleService {

  private final GameScheduleRepository repository;

  public List<GameSchedule> getByDate(String date) {
    return repository.findByGameDate(date);
  }

  public List<GameSchedule> getToday() {
    return repository.findByGameDate(LocalDate.now().toString());
  }

  public GameSchedule getById(String gameId) {
    return repository.findById(gameId).orElse(null);
  }
}
