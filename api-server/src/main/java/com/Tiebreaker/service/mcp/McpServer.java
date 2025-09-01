package com.Tiebreaker.service.mcp;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class McpServer extends TextWebSocketHandler {

  private final ObjectMapper objectMapper; // Spring 관리 ObjectMapper 주입 (JavaTimeModule 포함)
  private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

  private final PlayerStatsTool playerStatsTool;
  private final TeamRankingTool teamRankingTool;
  private final GameScheduleTool gameScheduleTool;

  private Map<String, McpTool> tools() {
    return Map.of(
        playerStatsTool.getName(), playerStatsTool,
        teamRankingTool.getName(), teamRankingTool,
        gameScheduleTool.getName(), gameScheduleTool);
  }

  @Override
  public void afterConnectionEstablished(WebSocketSession session) {
    sessions.put(session.getId(), session);
    try {
      McpMessage hello = new McpMessage("hello", "MCP server ready");
      session.sendMessage(new TextMessage(objectMapper.writeValueAsString(hello)));
    } catch (Exception e) {
      // 연결 실패 시 세션 제거
      sessions.remove(session.getId());
    }
  }

  @Override
  public void afterConnectionClosed(WebSocketSession session, org.springframework.web.socket.CloseStatus status) {
    sessions.remove(session.getId());
  }

  @Override
  protected void handleTextMessage(WebSocketSession session, TextMessage message) {
    long start = System.currentTimeMillis();
    String requestId = null;
    try {
      McpRequest request = objectMapper.readValue(message.getPayload(), McpRequest.class);
      requestId = request != null ? request.getRequestId() : null;
      
      if (request == null || !request.isValid()) {
        session.sendMessage(new TextMessage(objectMapper.writeValueAsString(
            McpResponse.invalidArguments(requestId, "유효하지 않은 요청"))));
        return;
      }

      switch (request.getType()) {
        case "tools/list": {
          var res = McpResponse.success(request.getRequestId(), tools().keySet());
          res.setExecutionTime(start);
          session.sendMessage(new TextMessage(objectMapper.writeValueAsString(res)));
          break;
        }
        case "tool/call": {
          String toolName = request.getToolName();
          McpTool tool = tools().get(toolName);
          if (tool == null) {
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(
                McpResponse.toolNotFound(request.getRequestId(), toolName))));
            return;
          }
          Object result = tool.execute(request.getArguments());
          var res = McpResponse.success(request.getRequestId(), result);
          res.setExecutionTime(start);
          session.sendMessage(new TextMessage(objectMapper.writeValueAsString(res)));
          break;
        }
        default: {
          session.sendMessage(new TextMessage(objectMapper.writeValueAsString(
              McpResponse.error(request.getRequestId(), "지원되지 않는 요청 타입: " + request.getType()))));
        }
      }
    } catch (Exception e) {
      try {
        session.sendMessage(new TextMessage(objectMapper.writeValueAsString(
            McpResponse.error(requestId, "요청 처리 중 오류: " + e.getMessage()))));
      } catch (Exception sendException) {
        // 로깅 추가
        System.err.println("응답 전송 실패: " + sendException.getMessage());
      }
    }
  }
}
