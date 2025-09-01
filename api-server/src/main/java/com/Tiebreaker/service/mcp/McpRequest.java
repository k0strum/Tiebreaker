package com.Tiebreaker.service.mcp;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * MCP 요청 클래스
 * 클라이언트에서 서버로 보내는 도구 호출 요청
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class McpRequest {

  /**
   * 요청 타입
   * - tool/call: 도구 호출
   * - tools/list: 도구 목록 요청
   */
  @JsonProperty("type")
  private String type;

  /**
   * 호출할 도구 이름
   * 예: "getPlayerStats", "getTeamRanking"
   */
  @JsonProperty("toolName")
  private String toolName;

  /**
   * 도구 실행에 필요한 매개변수들
   * Map 형태로 key-value 쌍으로 전달
   */
  @JsonProperty("arguments")
  private Map<String, Object> arguments;

  /**
   * 요청 ID
   * 요청과 응답을 매칭하기 위한 고유 식별자
   */
  @JsonProperty("requestId")
  private String requestId;

  // 세션 ID 필드 제거 - 현재 사용되지 않음

  /**
   * 도구 호출 요청 생성
   */
  public static McpRequest toolCall(String toolName, Map<String, Object> arguments, String requestId) {
    McpRequest request = new McpRequest();
    request.setType("tool/call");
    request.setToolName(toolName);
    request.setArguments(arguments);
    request.setRequestId(requestId);
    return request;
  }

  /**
   * 도구 목록 요청 생성
   */
  public static McpRequest toolsList(String requestId) {
    McpRequest request = new McpRequest();
    request.setType("tools/list");
    request.setRequestId(requestId);
    return request;
  }

  /**
   * 요청 유효성 검사
   */
  public boolean isValid() {
    // 기본 필수 필드 검증
    if (type == null || type.trim().isEmpty()) {
      return false;
    }
    
    if (requestId == null || requestId.trim().isEmpty()) {
      return false;
    }

    // 도구 호출 시 추가 검증
    if ("tool/call".equals(type)) {
      if (toolName == null || toolName.trim().isEmpty()) {
        return false;
      }
      
      // arguments가 null이 아닌 경우 Map 타입인지 확인
      if (arguments != null && !(arguments instanceof Map)) {
        return false;
      }
    }

    return true;
  }

  /**
   * 필수 매개변수 검사
   */
  public boolean hasRequiredArguments(String... requiredArgs) {
    if (arguments == null) {
      return requiredArgs.length == 0;
    }

    for (String arg : requiredArgs) {
      if (!arguments.containsKey(arg) || arguments.get(arg) == null) {
        return false;
      }
    }

    return true;
  }
}
