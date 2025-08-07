package com.Tiebreaker.service;

import com.Tiebreaker.dto.kboInfo.KboRankDto;
import com.Tiebreaker.service.kboInfo.RankService;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.io.IOException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
@RequiredArgsConstructor
public class KafkaConsumerService {

  private final ObjectMapper objectMapper;
  private final RankService rankService;

  @KafkaListener(topics = "kbo-rank-data", groupId = "tiebreak-group")
  public void consume(String message) {
    try {
      KboRankDto kboRankDto = objectMapper.readValue(message, KboRankDto.class);

      // DB 저장을 위해 RankService의 메서드 호출
      rankService.updateRanks(kboRankDto);
    } catch (JsonProcessingException e) {
      System.err.println("Error parsing JSON String: " + e);
    }
  }

}
