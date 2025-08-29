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
    } catch (Exception ignored) {
    }
  }

  @Override
  protected void handleTextMessage(WebSocketSession session, TextMessage message) {
    long start = System.currentTimeMillis();
    try {
      McpRequest request = objectMapper.readValue(message.getPayload(), McpRequest.class);
      if (request == null || !request.isValid()) {
        session.sendMessage(new TextMessage(objectMapper.writeValueAsString(
            McpResponse.invalidArguments(null, "유효하지 않은 요청"))));
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
            McpResponse.error(null, "요청 처리 중 오류: " + e.getMessage()))));
      } catch (Exception ignored) {
      }
    }
  }
}
