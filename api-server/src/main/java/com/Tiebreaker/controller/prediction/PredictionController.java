package com.Tiebreaker.controller.prediction;

import com.Tiebreaker.dto.prediction.*;
import com.Tiebreaker.entity.auth.Member;
import com.Tiebreaker.service.prediction.PredictionService;
import com.Tiebreaker.repository.auth.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/predictions")
@RequiredArgsConstructor
@Slf4j
public class PredictionController {

  private final PredictionService predictionService;
  private final MemberRepository memberRepository;

  /**
   * 오늘 예측 가능한 경기 목록 조회
   */
  @GetMapping("/today")
  public ResponseEntity<Map<String, Object>> getTodayPredictableGames() {
    try {
      List<GameInfoResponse> games = predictionService.getTodayPredictableGames();

      Map<String, Object> response = new HashMap<>();
      response.put("success", true);
      response.put("games", games);
      response.put("message", "오늘 예측 가능한 경기 목록을 조회했습니다.");

      return ResponseEntity.ok(response);
    } catch (Exception e) {
      log.error("오늘 예측 가능한 경기 목록 조회 실패: {}", e.getMessage(), e);
      Map<String, Object> response = new HashMap<>();
      response.put("success", false);
      response.put("message", "경기 목록 조회 중 오류가 발생했습니다.");
      return ResponseEntity.internalServerError().body(response);
    }
  }

  /**
   * 내 오늘 예측 목록 조회
   */
  @GetMapping("/my")
  public ResponseEntity<Map<String, Object>> getMyTodayPredictions() {
    try {
      Long memberId = getCurrentMemberId();
      List<PredictionResponse> predictions = predictionService.getUserTodayPredictions(memberId);

      Map<String, Object> response = new HashMap<>();
      response.put("success", true);
      response.put("predictions", predictions);
      response.put("message", "내 오늘 예측 목록을 조회했습니다.");

      return ResponseEntity.ok(response);
    } catch (Exception e) {
      log.error("내 오늘 예측 목록 조회 실패: {}", e.getMessage(), e);
      Map<String, Object> response = new HashMap<>();
      response.put("success", false);
      response.put("message", "예측 목록 조회 중 오류가 발생했습니다.");
      return ResponseEntity.internalServerError().body(response);
    }
  }

  /**
   * 예측 등록/수정
   */
  @PostMapping
  public ResponseEntity<Map<String, Object>> savePrediction(@Valid @RequestBody PredictionRequest request) {
    try {
      Long memberId = getCurrentMemberId();
      PredictionResponse prediction = predictionService.savePrediction(memberId, request);

      Map<String, Object> response = new HashMap<>();
      response.put("success", true);
      response.put("prediction", prediction);
      response.put("message", "예측이 성공적으로 저장되었습니다.");

      return ResponseEntity.ok(response);
    } catch (RuntimeException e) {
      log.error("예측 저장 실패: {}", e.getMessage());
      Map<String, Object> response = new HashMap<>();
      response.put("success", false);
      response.put("message", e.getMessage());
      return ResponseEntity.badRequest().body(response);
    } catch (Exception e) {
      log.error("예측 저장 중 오류 발생: {}", e.getMessage(), e);
      Map<String, Object> response = new HashMap<>();
      response.put("success", false);
      response.put("message", "예측 저장 중 오류가 발생했습니다.");
      return ResponseEntity.internalServerError().body(response);
    }
  }

  /**
   * 내 통계 조회
   */
  @GetMapping("/stats/my")
  public ResponseEntity<Map<String, Object>> getMyStatistics() {
    try {
      Long memberId = getCurrentMemberId();
      PredictionStatsResponse stats = predictionService.getUserStatistics(memberId);

      Map<String, Object> response = new HashMap<>();
      response.put("success", true);
      response.put("statistics", stats);
      response.put("message", "내 통계를 조회했습니다.");

      return ResponseEntity.ok(response);
    } catch (Exception e) {
      log.error("내 통계 조회 실패: {}", e.getMessage(), e);
      Map<String, Object> response = new HashMap<>();
      response.put("success", false);
      response.put("message", "통계 조회 중 오류가 발생했습니다.");
      return ResponseEntity.internalServerError().body(response);
    }
  }

  /**
   * 랭킹 조회
   */
  @GetMapping("/ranking")
  public ResponseEntity<Map<String, Object>> getRanking(
      @RequestParam String type,
      @RequestParam(required = false) String startDate,
      @RequestParam(required = false) String endDate) {
    try {
      LocalDate periodStart = startDate != null ? LocalDate.parse(startDate) : LocalDate.now();
      LocalDate periodEnd = endDate != null ? LocalDate.parse(endDate) : LocalDate.now();

      List<RankingResponse> ranking = predictionService.getRanking(type, periodStart, periodEnd);

      Map<String, Object> response = new HashMap<>();
      response.put("success", true);
      response.put("ranking", ranking);
      response.put("message", "랭킹을 조회했습니다.");

      return ResponseEntity.ok(response);
    } catch (Exception e) {
      log.error("랭킹 조회 실패: {}", e.getMessage(), e);
      Map<String, Object> response = new HashMap<>();
      response.put("success", false);
      response.put("message", "랭킹 조회 중 오류가 발생했습니다.");
      return ResponseEntity.internalServerError().body(response);
    }
  }

  /**
   * 일간 랭킹 조회
   */
  @GetMapping("/ranking/daily")
  public ResponseEntity<Map<String, Object>> getDailyRanking(
      @RequestParam(required = false) String date) {
    try {
      LocalDate targetDate = date != null ? LocalDate.parse(date) : LocalDate.now();
      List<RankingResponse> ranking = predictionService.getRanking("DAILY", targetDate, targetDate);

      Map<String, Object> response = new HashMap<>();
      response.put("success", true);
      response.put("ranking", ranking);
      response.put("message", "일간 랭킹을 조회했습니다.");

      return ResponseEntity.ok(response);
    } catch (Exception e) {
      log.error("일간 랭킹 조회 실패: {}", e.getMessage(), e);
      Map<String, Object> response = new HashMap<>();
      response.put("success", false);
      response.put("message", "일간 랭킹 조회 중 오류가 발생했습니다.");
      return ResponseEntity.internalServerError().body(response);
    }
  }

  /**
   * 월간 랭킹 조회
   */
  @GetMapping("/ranking/monthly")
  public ResponseEntity<Map<String, Object>> getMonthlyRanking(
      @RequestParam(required = false) String year,
      @RequestParam(required = false) String month) {
    try {
      int targetYear = year != null ? Integer.parseInt(year) : LocalDate.now().getYear();
      int targetMonth = month != null ? Integer.parseInt(month) : LocalDate.now().getMonthValue();

      LocalDate periodStart = LocalDate.of(targetYear, targetMonth, 1);
      LocalDate periodEnd = periodStart.withDayOfMonth(periodStart.lengthOfMonth());

      List<RankingResponse> ranking = predictionService.getRanking("MONTHLY", periodStart, periodEnd);

      Map<String, Object> response = new HashMap<>();
      response.put("success", true);
      response.put("ranking", ranking);
      response.put("message", "월간 랭킹을 조회했습니다.");

      return ResponseEntity.ok(response);
    } catch (Exception e) {
      log.error("월간 랭킹 조회 실패: {}", e.getMessage(), e);
      Map<String, Object> response = new HashMap<>();
      response.put("success", false);
      response.put("message", "월간 랭킹 조회 중 오류가 발생했습니다.");
      return ResponseEntity.internalServerError().body(response);
    }
  }

  /**
   * 연간 랭킹 조회
   */
  @GetMapping("/ranking/yearly")
  public ResponseEntity<Map<String, Object>> getYearlyRanking(
      @RequestParam(required = false) String year) {
    try {
      int targetYear = year != null ? Integer.parseInt(year) : LocalDate.now().getYear();

      LocalDate periodStart = LocalDate.of(targetYear, 1, 1);
      LocalDate periodEnd = LocalDate.of(targetYear, 12, 31);

      List<RankingResponse> ranking = predictionService.getRanking("YEARLY", periodStart, periodEnd);

      Map<String, Object> response = new HashMap<>();
      response.put("success", true);
      response.put("ranking", ranking);
      response.put("message", "연간 랭킹을 조회했습니다.");

      return ResponseEntity.ok(response);
    } catch (Exception e) {
      log.error("연간 랭킹 조회 실패: {}", e.getMessage(), e);
      Map<String, Object> response = new HashMap<>();
      response.put("success", false);
      response.put("message", "연간 랭킹 조회 중 오류가 발생했습니다.");
      return ResponseEntity.internalServerError().body(response);
    }
  }

  /**
   * 현재 로그인한 사용자 ID 조회
   * JWT 토큰에서 추출한 이메일로 DB에서 memberId 조회
   */
  private Long getCurrentMemberId() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null || !authentication.isAuthenticated()) {
      throw new RuntimeException("인증이 필요합니다.");
    }

    // JWT 토큰에서 추출한 이메일로 memberId 조회
    System.out.println("################################################");
    String email = authentication.getName();
    System.out.println(email);
    return memberRepository.findByEmail(email)
      .map(Member::getId)
      .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다: " + email));
  }
}
