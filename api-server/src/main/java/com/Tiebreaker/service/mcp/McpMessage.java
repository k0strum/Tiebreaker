package com.Tiebreaker.service.mcp;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * MCP 기본 메시지 클래스
 * 모든 MCP 통신의 기본이 되는 메시지 구조
 * 
 * 현재 McpServer에서는 McpRequest/McpResponse를 사용하므로
 * 이 클래스는 향후 확장성을 위해 유지됩니다.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class McpMessage {

  /**
   * 메시지 타입
   * - tools/list: 사용 가능한 도구 목록
   * - tool/call: 도구 호출 요청
   * - tool/result: 도구 실행 결과
   * - error: 오류 메시지
   */
  @JsonProperty("type")
  private String type;

  /**
   * 메시지 내용
   * 타입에 따라 다른 형태의 데이터를 담을 수 있음
   */
  @JsonProperty("content")
  private Object content;

  /**
   * 요청 ID
   * 요청과 응답을 매칭하기 위한 고유 식별자
   */
  @JsonProperty("requestId")
  private String requestId;

  /**
   * 오류 정보 (선택적)
   */
  @JsonProperty("error")
  private String error;

  /**
   * 타임스탬프
   */
  @JsonProperty("timestamp")
  private Long timestamp;

  /**
   * 기본 생성자
   */
  public McpMessage(String type, Object content) {
    this.type = type;
    this.content = content;
    this.timestamp = System.currentTimeMillis();
  }

  /**
   * 오류 메시지 생성
   */
  public static McpMessage error(String error, String requestId) {
    McpMessage message = new McpMessage();
    message.setType("error");
    message.setError(error);
    message.setRequestId(requestId);
    message.setTimestamp(System.currentTimeMillis());
    return message;
  }

  /**
   * 도구 목록 메시지 생성
   */
  public static McpMessage toolsList(Object tools) {
    return new McpMessage("tools/list", tools);
  }

  /**
   * 도구 결과 메시지 생성
   */
  public static McpMessage toolResult(Object result, String requestId) {
    McpMessage message = new McpMessage("tool/result", result);
    message.setRequestId(requestId);
    return message;
  }
}
