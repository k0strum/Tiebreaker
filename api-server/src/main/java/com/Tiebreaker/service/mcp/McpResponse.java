package com.Tiebreaker.service.mcp;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * MCP 응답 클래스
 * 서버에서 클라이언트로 보내는 도구 실행 결과
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class McpResponse {

  /**
   * 응답 타입
   * - tool/result: 도구 실행 결과
   * - error: 오류 응답
   * - success: 성공 응답
   */
  @JsonProperty("type")
  private String type;

  /**
   * 요청 ID
   * 원본 요청과 매칭하기 위한 식별자
   */
  @JsonProperty("requestId")
  private String requestId;

  /**
   * 응답 내용
   * 도구 실행 결과 또는 오류 메시지
   */
  @JsonProperty("content")
  private Object content;

  /**
   * 오류 정보
   */
  @JsonProperty("error")
  private String error;

  /**
   * 성공 여부
   */
  @JsonProperty("success")
  private Boolean success;

  /**
   * 실행 시간 (밀리초)
   */
  @JsonProperty("executionTime")
  private Long executionTime;

  /**
   * 타임스탬프
   */
  @JsonProperty("timestamp")
  private Long timestamp;

  /**
   * 성공 응답 생성
   */
  public static McpResponse success(String requestId, Object content) {
    McpResponse response = new McpResponse();
    response.setType("tool/result");
    response.setRequestId(requestId);
    response.setContent(content);
    response.setSuccess(true);
    response.setTimestamp(System.currentTimeMillis());
    return response;
  }

  /**
   * 오류 응답 생성
   */
  public static McpResponse error(String requestId, String error) {
    McpResponse response = new McpResponse();
    response.setType("error");
    response.setRequestId(requestId);
    response.setError(error);
    response.setSuccess(false);
    response.setTimestamp(System.currentTimeMillis());
    return response;
  }

  /**
   * 도구를 찾을 수 없는 경우
   */
  public static McpResponse toolNotFound(String requestId, String toolName) {
    return error(requestId, "도구를 찾을 수 없습니다: " + toolName);
  }

  /**
   * 매개변수 오류
   */
  public static McpResponse invalidArguments(String requestId, String message) {
    return error(requestId, "잘못된 매개변수: " + message);
  }

  /**
   * 실행 시간 설정
   */
  public void setExecutionTime(long startTime) {
    this.executionTime = System.currentTimeMillis() - startTime;
  }

  /**
   * 응답 유효성 검사
   */
  public boolean isValid() {
    return type != null && requestId != null && timestamp != null;
  }
}
