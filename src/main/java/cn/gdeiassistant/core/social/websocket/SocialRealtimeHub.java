package cn.gdeiassistant.core.social.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * 单实例本机连接管理；成功事件仅在事务 afterCommit 推送，rollback 零成功事件。
 */
@Component
public class SocialRealtimeHub {

    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(SocialRealtimeHub.class);

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ConcurrentHashMap<Long, Set<WebSocketSession>> userSessions = new ConcurrentHashMap<>();

    public void register(long userId, WebSocketSession session) {
        userSessions.compute(userId, (id, sessions) -> {
            if (sessions == null) {
                sessions = new CopyOnWriteArraySet<>();
            }
            sessions.add(session);
            return sessions;
        });
    }

    public void unregister(long userId, WebSocketSession session) {
        userSessions.computeIfPresent(userId, (id, sessions) -> {
            sessions.remove(session);
            return sessions.isEmpty() ? null : sessions;
        });
    }

    public void disconnectUser(long userId) {
        Set<WebSocketSession> sessions = userSessions.remove(userId);
        if (sessions == null) {
            return;
        }
        for (WebSocketSession session : sessions) {
            closeQuietly(session);
        }
    }

    public void disconnectSession(WebSocketSession session) {
        Object userId = session.getAttributes().get("userId");
        if (userId instanceof Number) {
            unregister(((Number) userId).longValue(), session);
        }
        closeQuietly(session);
    }

    /**
     * 事务内调用时延迟到 afterCommit；无事务时立即推送。
     */
    public void pushToUserAfterCommit(long userId, Map<String, Object> event) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    pushToUser(userId, event);
                }
            });
        } else {
            pushToUser(userId, event);
        }
    }

    public void pushToUser(long userId, Map<String, Object> event) {
        Set<WebSocketSession> sessions = userSessions.get(userId);
        if (sessions == null || sessions.isEmpty()) {
            return;
        }
        String payload;
        try {
            payload = objectMapper.writeValueAsString(event);
        } catch (IOException e) {
            return;
        }
        TextMessage message = new TextMessage(payload);
        for (WebSocketSession session : sessions) {
            if (!sendToSession(session, message)) unregister(userId, session);
        }
    }

    /** All application frames, including ready/pong, use the same session monitor. */
    public boolean sendToSession(WebSocketSession session, TextMessage message) {
        synchronized (session) {
            try {
                if (!session.isOpen() || !Boolean.TRUE.equals(session.getAttributes().get("authenticated"))) {
                    return false;
                }
                session.sendMessage(message);
                return true;
            } catch (IOException | IllegalStateException failure) {
                LOGGER.warn("Realtime send failed: {}", failure.getClass().getSimpleName());
            }
        }
        // A transport failure affects this peer only, never the committed business result.
        disconnectSession(session);
        return false;
    }

    private void closeQuietly(WebSocketSession session) {
        try {
            if (session.isOpen()) {
                session.close();
            }
        } catch (IOException | IllegalStateException failure) {
            LOGGER.warn("Realtime close failed: {}", failure.getClass().getSimpleName());
        }
    }
}
