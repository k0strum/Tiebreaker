package com.Tiebreaker.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

  @Value("${app.frontend-url:http://localhost:5173}")
  private String frontendUrl;

  @Override
  public void registerStompEndpoints(StompEndpointRegistry registry) {
    // 클라이언트가 연결할 STOMP 엔드포인트
    // SockJS 전용(브라우저 폴백)
    registry.addEndpoint("/ws")
        .setAllowedOrigins(frontendUrl, "http://localhost:5173")
        .withSockJS();

    // 네이티브 WebSocket 전용(포스트맨/원 WS 클라이언트)
    registry.addEndpoint("/ws-native")
        .setAllowedOrigins(frontendUrl, "http://localhost:5173");
  }

  @Override
  public void configureMessageBroker(MessageBrokerRegistry registry) {
    // 클라이언트 -> 서버
    registry.setApplicationDestinationPrefixes("/app");

    // 서버 -> 클라이언트
    registry.enableSimpleBroker("/topic");
  }
}
