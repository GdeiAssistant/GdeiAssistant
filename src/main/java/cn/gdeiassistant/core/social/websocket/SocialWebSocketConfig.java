package cn.gdeiassistant.core.social.websocket;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class SocialWebSocketConfig implements WebSocketConfigurer {

    private final SocialRealtimeHandler socialRealtimeHandler;

    public SocialWebSocketConfig(SocialRealtimeHandler socialRealtimeHandler) {
        this.socialRealtimeHandler = socialRealtimeHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(socialRealtimeHandler, "/api/social/realtime")
                .setAllowedOriginPatterns("*");
    }
}
