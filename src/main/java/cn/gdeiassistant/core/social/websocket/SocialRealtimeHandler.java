package cn.gdeiassistant.core.social.websocket;

import cn.gdeiassistant.common.pojo.entity.User;
import cn.gdeiassistant.common.redis.usercertificate.UserCertificateDao;
import cn.gdeiassistant.common.tools.utils.JwtUtil;
import cn.gdeiassistant.core.user.mapper.UserMapper;
import cn.gdeiassistant.core.user.pojo.entity.CampusAccountView;
import com.auth0.jwt.interfaces.Claim;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * /api/social/realtime：首帧 auth，5 秒未认证关闭；校验签名/过期/会话撤销/账号状态。
 * 认证后有界周期复核 Redis 会话与账号状态，过期/撤销则关闭连接。
 */
@Component
public class SocialRealtimeHandler extends TextWebSocketHandler {

    private static final long AUTH_TIMEOUT_SECONDS = 5L;
    private static final long SESSION_RECHECK_SECONDS = 30L;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "social-ws-session");
        t.setDaemon(true);
        return t;
    });
    private final ConcurrentHashMap<String, ScheduledFuture<?>> authTimeouts = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, ScheduledFuture<?>> sessionRechecks = new ConcurrentHashMap<>();

    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private UserCertificateDao userCertificateDao;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private SocialRealtimeHub realtimeHub;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        ScheduledFuture<?> future = scheduler.schedule(() -> {
            if (!Boolean.TRUE.equals(session.getAttributes().get("authenticated"))) {
                closeQuietly(session, CloseStatus.POLICY_VIOLATION);
            }
        }, AUTH_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        authTimeouts.put(session.getId(), future);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        JsonNode root;
        try {
            root = objectMapper.readTree(message.getPayload());
        } catch (Exception e) {
            session.close(CloseStatus.BAD_DATA);
            return;
        }
        String type = root.path("type").asText("");
        if ("auth".equals(type)) {
            handleAuth(session, root.path("token").asText(null));
            return;
        }
        if (!Boolean.TRUE.equals(session.getAttributes().get("authenticated"))) {
            session.close(CloseStatus.POLICY_VIOLATION);
            return;
        }
        if ("ping".equals(type)) {
            if (!sessionStillValid(session)) {
                revokeAndClose(session);
                return;
            }
            if (!realtimeHub.sendToSession(session, new TextMessage("{\"type\":\"pong\"}"))) {
                cancelSessionRecheck(session.getId());
            }
        }
    }

    private void handleAuth(WebSocketSession session, String token) throws IOException {
        if (Boolean.TRUE.equals(session.getAttributes().get("authenticated"))) {
            return;
        }
        if (token == null || token.isBlank()) {
            session.close(CloseStatus.POLICY_VIOLATION);
            return;
        }
        try {
            Map<String, Claim> claims = jwtUtil.verifyAndParse(token);
            Claim sessionClaim = claims.get("sessionId");
            Claim usernameClaim = claims.get("username");
            if (sessionClaim == null || usernameClaim == null) {
                session.close(CloseStatus.POLICY_VIOLATION);
                return;
            }
            String sessionId = sessionClaim.asString();
            String username = usernameClaim.asString();
            User login = userCertificateDao.queryUserLoginCertificate(sessionId);
            if (login == null || login.getUsername() == null || !login.getUsername().equals(username)) {
                session.close(CloseStatus.POLICY_VIOLATION);
                return;
            }
            CampusAccountView appUser = userMapper.selectUser(username);
            if (appUser == null || !appUser.isActive()) {
                session.close(CloseStatus.POLICY_VIOLATION);
                return;
            }
            session.getAttributes().put("authenticated", true);
            session.getAttributes().put("userId", appUser.getId());
            session.getAttributes().put("sessionId", sessionId);
            session.getAttributes().put("username", username);
            Claim expiryClaim = claims.get("exp");
            Long expiresAt = expiryClaim == null ? null : expiryClaim.asLong();
            if (expiresAt == null || expiresAt <= java.time.Instant.now().getEpochSecond()) {
                session.getAttributes().put("authenticated", false);
                session.close(CloseStatus.POLICY_VIOLATION);
                return;
            }
            session.getAttributes().put("expiresAt", expiresAt);
            cancelAuthTimeout(session.getId());
            synchronized (session) {
                // Hold the send monitor before registration so ready precedes business frames.
                realtimeHub.register(appUser.getId(), session);
                scheduleSessionRecheck(session);
                if (!realtimeHub.sendToSession(session, new TextMessage("{\"type\":\"ready\"}"))) {
                    cancelSessionRecheck(session.getId());
                }
            }
        } catch (Exception e) {
            session.close(CloseStatus.POLICY_VIOLATION);
        }
    }

    private void scheduleSessionRecheck(WebSocketSession session) {
        cancelSessionRecheck(session.getId());
        ScheduledFuture<?> future = scheduler.scheduleAtFixedRate(() -> {
            if (!session.isOpen()) {
                cancelSessionRecheck(session.getId());
                return;
            }
            if (!sessionStillValid(session)) {
                revokeAndClose(session);
            }
        }, SESSION_RECHECK_SECONDS, SESSION_RECHECK_SECONDS, TimeUnit.SECONDS);
        sessionRechecks.put(session.getId(), future);
    }

    private boolean sessionStillValid(WebSocketSession session) {
        Object expiry = session.getAttributes().get("expiresAt");
        if (!(expiry instanceof Number) || ((Number) expiry).longValue() <= java.time.Instant.now().getEpochSecond()) {
            return false;
        }
        Object sessionIdObj = session.getAttributes().get("sessionId");
        Object usernameObj = session.getAttributes().get("username");
        Object userIdObj = session.getAttributes().get("userId");
        if (!(sessionIdObj instanceof String sessionId) || !(usernameObj instanceof String username)
                || !(userIdObj instanceof Number)) {
            return false;
        }
        User login;
        try {
            login = userCertificateDao.queryUserLoginCertificate(sessionId);
        } catch (RuntimeException ex) {
            return false;
        }
        if (login == null || login.getUsername() == null || !login.getUsername().equals(username)) {
            return false;
        }
        try {
            CampusAccountView appUser = userMapper.selectUserById(((Number) userIdObj).longValue());
            return appUser != null && appUser.isActive();
        } catch (RuntimeException ex) {
            return false;
        }
    }

    private void revokeAndClose(WebSocketSession session) {
        cancelSessionRecheck(session.getId());
        Object userId = session.getAttributes().get("userId");
        if (userId instanceof Number) {
            realtimeHub.unregister(((Number) userId).longValue(), session);
        }
        session.getAttributes().put("authenticated", false);
        closeQuietly(session, CloseStatus.POLICY_VIOLATION);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        cancelAuthTimeout(session.getId());
        cancelSessionRecheck(session.getId());
        Object userId = session.getAttributes().get("userId");
        if (userId instanceof Long) {
            realtimeHub.unregister((Long) userId, session);
        } else if (userId instanceof Number) {
            realtimeHub.unregister(((Number) userId).longValue(), session);
        }
    }

    @PreDestroy
    public void destroy() {
        for (ScheduledFuture<?> future : authTimeouts.values()) {
            future.cancel(false);
        }
        authTimeouts.clear();
        for (ScheduledFuture<?> future : sessionRechecks.values()) {
            future.cancel(false);
        }
        sessionRechecks.clear();
        scheduler.shutdownNow();
    }

    private void cancelAuthTimeout(String sessionId) {
        ScheduledFuture<?> future = authTimeouts.remove(sessionId);
        if (future != null) {
            future.cancel(false);
        }
    }

    private void cancelSessionRecheck(String sessionId) {
        ScheduledFuture<?> future = sessionRechecks.remove(sessionId);
        if (future != null) {
            future.cancel(false);
        }
    }

    private void closeQuietly(WebSocketSession session, CloseStatus status) {
        try {
            if (session.isOpen()) {
                session.close(status);
            }
        } catch (IOException | IllegalStateException ignored) {
        }
    }
}
