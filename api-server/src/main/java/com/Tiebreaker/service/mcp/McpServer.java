// package com.Tiebreaker.service.mcp;
//
// import com.fasterxml.jackson.databind.ObjectMapper;
// import org.springframework.stereotype.Service;
// import org.springframework.web.socket.TextMessage;
// import org.springframework.web.socket.WebSocketSession;
// import org.springframework.web.socket.handler.TextWebSocketHandler;
//
// import java.util.Map;
// import java.util.concurrent.ConcurrentHashMap;
//
// @Service
// public class McpServer extends TextWebSocketHandler {
//
// private final ObjectMapper objectMapper = new ObjectMapper();
// private final Map<String, WebSocketSession> sessions = new
// ConcurrentHashMap<>();
//
// // MCP 도구들 정의
// private final Map<String, McpTool> tools = Map.of(
// "getPlayerStats", new PlayerStatsTool(),
// "getTeamRanking", new TeamRankingTool(),
// "getGameSchedule", new GameScheduleTool(),
// "getPlayerRanking", new PlayerRankingTool());
//
// @Override
// public void afterConnectionEstablished(WebSocketSession session) {
// sessions.put(session.getId(), session);
// // MCP 초기화 메시지 전송
// sendInitialization(session);
// }
//
// private void sendInitialization(WebSocketSession session) {
// try {
// // 사용 가능한 도구 목록 전송
// McpMessage initMessage = new McpMessage();
// initMessage.setType("tools/list");
// initMessage.setContent(tools.keySet());
//
// session.sendMessage(new
// TextMessage(objectMapper.writeValueAsString(initMessage)));
// } catch (Exception e) {
// e.printStackTrace();
// }
// }
//
// @Override
// protected void handleTextMessage(WebSocketSession session, TextMessage
// message) {
// try {
// McpRequest request = objectMapper.readValue(message.getPayload(),
// McpRequest.class);
//
// // 도구 호출 처리
// if ("tool/call".equals(request.getType())) {
// handleToolCall(session, request);
// }
//
// } catch (Exception e) {
// e.printStackTrace();
// }
// }
//
// private void handleToolCall(WebSocketSession session, McpRequest request) {
// try {
// String toolName = request.getToolName();
// McpTool tool = tools.get(toolName);
//
// if (tool != null) {
// // 도구 실행
// Object result = tool.execute(request.getArguments());
//
// // 결과 전송
// McpResponse response = new McpResponse();
// response.setType("tool/result");
// response.setRequestId(request.getRequestId());
// response.setContent(result);
//
// session.sendMessage(new
// TextMessage(objectMapper.writeValueAsString(response)));
// }
//
// } catch (Exception e) {
// e.printStackTrace();
// }
// }
// }
