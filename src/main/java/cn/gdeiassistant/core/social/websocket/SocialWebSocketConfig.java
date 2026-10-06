package cn.gdeiassistant.core.social.websocket;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class SocialWebSocketConfig implements WebSocketConfigurer {

    @Autowired
    private SocialRealtimeHandler socialRealtimeHandler;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(socialRealtimeHandler, "/api/social/realtime")
                .setAllowedOriginPatterns("*");
    }
}
