package com.Tiebreaker.service.kboInfoCollecting;

import com.Tiebreaker.dto.kboInfo.KboRankDto;
import com.Tiebreaker.service.kboInfoCollecting.daily.RankCollectService;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

@Service
@RequiredArgsConstructor
public class KafkaConsumerService {

  private final ObjectMapper objectMapper;
  private final RankCollectService rankCollectService;

  @KafkaListener(topics = "kbo-rank-data", groupId = "tiebreak-group")
  public void consume(String message) {
    try {
      KboRankDto kboRankDto = objectMapper.readValue(message, KboRankDto.class);

      // DB 저장을 위해 RankService의 메서드 호출
      rankCollectService.updateRanks(kboRankDto);
    } catch (JsonProcessingException e) {
      System.err.println("Error parsing JSON String: " + e);
    }
  }

}
