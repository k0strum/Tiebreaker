package com.Tiebreaker.dto.prediction;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 예측 등록/수정 요청 DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PredictionRequest {

  @NotBlank(message = "경기 ID는 필수입니다.")
  private String gameId;

  @NotBlank(message = "예측 승자는 필수입니다.")
  @Pattern(regexp = "^(HOME|AWAY)$", message = "예측 승자는 HOME 또는 AWAY만 가능합니다.")
  private String predictedWinner;
}
