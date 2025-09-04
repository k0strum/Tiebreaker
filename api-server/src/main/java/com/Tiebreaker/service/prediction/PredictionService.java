package com.Tiebreaker.service.prediction;

import com.Tiebreaker.entity.prediction.Prediction;
import com.Tiebreaker.entity.prediction.PredictionInfo;
import com.Tiebreaker.entity.prediction.PredictionRanking;
import com.Tiebreaker.entity.auth.Member;
import com.Tiebreaker.entity.livegame.LiveGameInfo;
import com.Tiebreaker.dto.prediction.*;
import com.Tiebreaker.repository.Prediction.PredictionRepository;
import com.Tiebreaker.repository.Prediction.PredictionInfoRepository;
import com.Tiebreaker.repository.Prediction.PredictionRankingRepository;
import com.Tiebreaker.repository.MemberRepository;
import com.Tiebreaker.repository.LiveGameInfoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PredictionService {

  private final PredictionRepository predictionRepository;
  private final PredictionInfoRepository predictionInfoRepository;
  private final PredictionRankingRepository predictionRankingRepository;
  private final MemberRepository memberRepository;
  private final LiveGameInfoRepository liveGameInfoRepository;

  /**
   * 오늘 예측 가능한 경기 목록 조회
   */
  @Transactional(readOnly = true)
  public List<GameInfoResponse> getTodayPredictableGames() {
    LocalDate today = LocalDate.now();
    List<LiveGameInfo> games = liveGameInfoRepository.findTodayActiveGames(today);
    return games.stream()
        .map(this::convertToGameInfoResponse)
        .toList();
  }

  /**
   * 사용자의 오늘 예측 목록 조회
   */
  @Transactional(readOnly = true)
  public List<PredictionResponse> getUserTodayPredictions(Long memberId) {
    Member member = memberRepository.findById(memberId)
        .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
    List<Prediction> predictions = predictionRepository.findByMemberAndPredictionDate(member, LocalDate.now());
    return predictions.stream()
        .map(this::convertToPredictionResponse)
        .toList();
  }

  /**
   * 예측 등록/수정
   */
  @Transactional
  public PredictionResponse savePrediction(Long memberId, PredictionRequest request) {
    Member member = memberRepository.findById(memberId)
        .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

    LiveGameInfo game = liveGameInfoRepository.findById(request.getGameId())
        .orElseThrow(() -> new RuntimeException("경기를 찾을 수 없습니다."));

    LocalDate today = LocalDate.now();

    // 예측 마감 시간 확인 (경기 시작 30분 전)
    if (isPredictionClosed(game)) {
      throw new RuntimeException("예측 마감 시간이 지났습니다.");
    }

    // 기존 예측 조회
    Optional<Prediction> existingPrediction = predictionRepository
        .findByMemberAndGameAndPredictionDate(member, game, today);

    Prediction prediction;
    if (existingPrediction.isPresent()) {
      // 기존 예측 수정
      prediction = existingPrediction.get();
      prediction.setPredictedWinner(request.getPredictedWinner());
      log.info("예측 수정: {} - {} vs {} (예측: {})",
          member.getNickname(), game.getHomeTeam(), game.getAwayTeam(), request.getPredictedWinner());
    } else {
      // 새 예측 생성
      prediction = new Prediction();
      prediction.setMember(member);
      prediction.setGame(game);
      prediction.setPredictedWinner(request.getPredictedWinner());
      prediction.setPredictionDate(today);
      log.info("예측 등록: {} - {} vs {} (예측: {})",
          member.getNickname(), game.getHomeTeam(), game.getAwayTeam(), request.getPredictedWinner());
    }

    Prediction savedPrediction = predictionRepository.save(prediction);
    return convertToPredictionResponse(savedPrediction);
  }

  /**
   * 예측 마감 시간 확인
   */
  private boolean isPredictionClosed(LiveGameInfo game) {
    // 경기 시작 시간이 설정되어 있다면 30분 전 마감
    // 현재는 단순히 현재 시간 기준으로 판단
    return false; // TODO: 실제 경기 시간과 비교하여 구현
  }

  /**
   * 경기 결과 처리 및 마일리지 지급
   */
  @Transactional
  public void processGameResults(LocalDate date) {
    List<Prediction> predictions = predictionRepository.findByPredictionDate(date);

    for (Prediction prediction : predictions) {
      processPredictionResult(prediction);
    }

    // 일일 결과 요약 생성
    createDailyPredictionInfo(date);

    log.info("경기 결과 처리 완료: {} - {}개 예측 처리", date, predictions.size());
  }

  /**
   * 개별 예측 결과 처리
   */
  private void processPredictionResult(Prediction prediction) {
    LiveGameInfo game = prediction.getGame();

    // 경기 결과 확인
    if ("FINISHED".equals(game.getStatus())) {
      String actualWinner = determineWinner(game);
      boolean isCorrect = prediction.getPredictedWinner().equals(actualWinner);

      // 예측 결과 저장 (필요시)
      log.info("예측 결과: {} - {} vs {} (예측: {}, 실제: {}, 정답: {})",
          prediction.getMember().getNickname(),
          game.getHomeTeam(), game.getAwayTeam(),
          prediction.getPredictedWinner(), actualWinner, isCorrect);
    }
  }

  /**
   * 경기 승자 결정
   */
  private String determineWinner(LiveGameInfo game) {
    if (game.getHomeScore() > game.getAwayScore()) {
      return "HOME";
    } else if (game.getAwayScore() > game.getHomeScore()) {
      return "AWAY";
    } else {
      return "DRAW"; // 무승부 (보너스 마일리지 지급)
    }
  }

  /**
   * 일일 예측 결과 요약 생성
   */
  private void createDailyPredictionInfo(LocalDate date) {
    List<Prediction> predictions = predictionRepository.findByPredictionDate(date);

    // 사용자별로 그룹화하여 결과 요약
    predictions.stream()
        .collect(java.util.stream.Collectors.groupingBy(Prediction::getMember))
        .forEach((member, memberPredictions) -> {
          createMemberPredictionInfo(member, memberPredictions, date);
        });
  }

  /**
   * 사용자별 예측 결과 요약 생성
   */
  private void createMemberPredictionInfo(Member member, List<Prediction> predictions, LocalDate date) {
    int totalGames = predictions.size();
    int correctPredictions = 0;
    int bonusMileage = 0;

    for (Prediction prediction : predictions) {
      LiveGameInfo game = prediction.getGame();

      if ("FINISHED".equals(game.getStatus())) {
        String actualWinner = determineWinner(game);
        if ("DRAW".equals(actualWinner)) {
          // 무승부 보상
          bonusMileage += 10;
        } else if (prediction.getPredictedWinner().equals(actualWinner)) {
          correctPredictions++;
        }
      } else if ("CANCELLED".equals(game.getStatus()) || "POSTPONED".equals(game.getStatus())) {
        // 취소/연기 보상
        bonusMileage += 10;
      }
    }

    int earnedMileage = calculateMileage(correctPredictions, totalGames);
    int totalMileage = earnedMileage + bonusMileage;

    // PredictionInfo 생성
    PredictionInfo predictionInfo = new PredictionInfo();
    predictionInfo.setMember(member);
    predictionInfo.setPredictionDate(date);
    predictionInfo.setTotalGames(totalGames);
    predictionInfo.setCorrectPredictions(correctPredictions);
    predictionInfo.setEarnedMileage(earnedMileage);
    predictionInfo.setBonusMileage(bonusMileage);
    predictionInfo.setTotalMileage(totalMileage);

    predictionInfoRepository.save(predictionInfo);

    // 사용자 마일리지 업데이트
    member.setMileage(member.getMileage() + totalMileage);
    memberRepository.save(member);

    log.info("예측 결과 요약: {} - {}경기 중 {}정답, {}마일리지 획득",
        member.getNickname(), totalGames, correctPredictions, totalMileage);
  }

  /**
   * 마일리지 계산
   */
  private int calculateMileage(int correctCount, int totalGames) {
    // 고정 테이블 방식 (경기 수와 무관, 적중 개수로만 결정)
    // 0,1,2,3,4,5개 적중 시 각각 0/10/25/45/70/100 점
    // 6개 이상 적중 시 초과 1개당 +30점 가산
    int[] baseForFive = { 0, 10, 25, 45, 70, 100 };
    if (correctCount <= 5) {
      int idx = correctCount;
      if (idx < 0)
        idx = 0;
      return baseForFive[idx];
    }
    int extra = correctCount - 5;
    return baseForFive[5] + (extra * 30);
  }

  /**
   * 사용자 통계 조회
   */
  @Transactional(readOnly = true)
  public PredictionStatsResponse getUserStatistics(Long memberId) {
    Member member = memberRepository.findById(memberId)
        .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
    Object[] stats = predictionInfoRepository.getMemberStatistics(member);

    if (stats == null || stats.length < 4) {
      return PredictionStatsResponse.builder()
          .totalPredictions(0L)
          .totalCorrect(0L)
          .totalGames(0L)
          .totalMileage(0L)
          .accuracy(0.0)
          .build();
    }

    Long totalPredictions = (Long) stats[0];
    Long totalCorrect = (Long) stats[1];
    Long totalGames = (Long) stats[2];
    Long totalMileage = (Long) stats[3];
    Double accuracy = totalGames > 0 ? (double) totalCorrect / totalGames : 0.0;

    return PredictionStatsResponse.builder()
        .totalPredictions(totalPredictions)
        .totalCorrect(totalCorrect)
        .totalGames(totalGames)
        .totalMileage(totalMileage)
        .accuracy(accuracy)
        .build();
  }

  /**
   * 랭킹 조회
   */
  @Transactional(readOnly = true)
  public List<RankingResponse> getRanking(String rankingType, LocalDate periodStart, LocalDate periodEnd) {
    List<PredictionRanking> rankings = predictionRankingRepository.findRankingByTypeAndPeriod(rankingType, periodStart,
        periodEnd);
    return rankings.stream()
        .map(this::convertToRankingResponse)
        .toList();
  }

  // DTO 변환 메서드들
  private GameInfoResponse convertToGameInfoResponse(LiveGameInfo game) {
    return GameInfoResponse.builder()
        .gameId(game.getGameId())
        .homeTeam(game.getHomeTeam())
        .awayTeam(game.getAwayTeam())
        .status(game.getStatus())
        .gameDateTime(null) // TODO: 실제 경기 시간 설정
        .isPredictable("READY".equals(game.getStatus()))
        .predictionDeadline(null) // TODO: 예측 마감 시간 계산
        .build();
  }

  private PredictionResponse convertToPredictionResponse(Prediction prediction) {
    return PredictionResponse.builder()
        .id(prediction.getId())
        .gameId(prediction.getGame().getGameId())
        .homeTeam(prediction.getGame().getHomeTeam())
        .awayTeam(prediction.getGame().getAwayTeam())
        .predictedWinner(prediction.getPredictedWinner())
        .predictionDate(prediction.getPredictionDate())
        .createdAt(prediction.getRegDate())
        .updatedAt(prediction.getUpdateDate())
        .build();
  }

  private RankingResponse convertToRankingResponse(PredictionRanking ranking) {
    return RankingResponse.builder()
        .memberId(ranking.getMember().getId())
        .nickname(ranking.getMember().getNickname())
        .profileImage(ranking.getMember().getProfileImage())
        .totalCorrect(ranking.getTotalCorrect())
        .totalGames(ranking.getTotalGames())
        .accuracy(ranking.getAccuracy())
        .totalMileage(ranking.getTotalMileage())
        .rankingPosition(ranking.getRankingPosition())
        .rankingType(ranking.getRankingType())
        .periodStart(ranking.getPeriodStart())
        .periodEnd(ranking.getPeriodEnd())
        .build();
  }
}
