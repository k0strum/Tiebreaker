package com.Tiebreaker.service.mcp;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

public class McpGatewayHandler extends TextWebSocketHandler {
  private final ObjectMapper objectMapper = new ObjectMapper();

  @Override
  public void afterConnectionEstablished(WebSocketSession session) throws Exception {
    // 연결 확인용 웰컴 메시지
    McpMessage hello = new McpMessage("hello", "MCP connection established");
    session.sendMessage(new TextMessage(objectMapper.writeValueAsString(hello)));
  }

  @Override
  protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
    // 아주 기초적인 tools/list 처리만 제공 (빈 목록)
    McpRequest req = objectMapper.readValue(message.getPayload(), McpRequest.class);
    if ("tools/list".equals(req.getType())) {
      McpResponse res = McpResponse.success(req.getRequestId(), java.util.List.of());
      session.sendMessage(new TextMessage(objectMapper.writeValueAsString(res)));
      return;
    }
    // 추후 McpServer로 교체되면 여긴 실행 경로에서 제외됨
    McpResponse res = McpResponse.error(req.getRequestId(), "지원되지 않는 요청 타입: " + req.getType());
    session.sendMessage(new TextMessage(objectMapper.writeValueAsString(res)));
  }
}