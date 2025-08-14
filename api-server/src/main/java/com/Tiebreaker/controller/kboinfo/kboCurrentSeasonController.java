package com.Tiebreaker.controller.kboinfo;

import com.Tiebreaker.entity.kboInfo.Player;
import com.Tiebreaker.service.kboinfo.KboPlayerService;
import com.Tiebreaker.service.kboinfo.KboTeamService;
import com.Tiebreaker.service.ImageService;
import com.Tiebreaker.dto.kboInfo.CurrentTeamRankResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/info/current")
@RequiredArgsConstructor
@Slf4j
public class kboCurrentSeasonController {

  private final KboPlayerService kboPlayerService;
  private final KboTeamService kboTeamService;
  private final ImageService imageService;

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
