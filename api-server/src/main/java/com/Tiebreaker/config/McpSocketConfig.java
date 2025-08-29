package com.Tiebreaker.config;

import com.Tiebreaker.service.mcp.McpServer;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class McpSocketConfig implements WebSocketConfigurer {

  private final McpServer mcpServer;

  public McpSocketConfig(McpServer mcpServer) {
    this.mcpServer = mcpServer;
  }

  @Override
  public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
    registry.addHandler(mcpServer, "/mcp")
        .setAllowedOriginPatterns("*");
  }
}