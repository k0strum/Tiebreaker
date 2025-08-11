package com.Tiebreaker.controller.kboinfo;

import com.Tiebreaker.service.kboinfo.KboTeamService;
import com.Tiebreaker.dto.kboInfo.CurrentTeamRankResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/info/current")
@RequiredArgsConstructor
public class kboCurrentSeasonController {

  private final KboTeamService kboTeamService;

  @GetMapping("/teamRank")
  public ResponseEntity<List<CurrentTeamRankResponseDto>> getTeamRank() {
    try {
      List<CurrentTeamRankResponseDto> teamRanks = kboTeamService.getTeamRank();
      return ResponseEntity.ok(teamRanks);
    } catch (Exception e) {
      return ResponseEntity.internalServerError().build();
    }
  }

}
