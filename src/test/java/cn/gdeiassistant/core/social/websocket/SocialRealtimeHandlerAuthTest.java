package cn.gdeiassistant.core.social.websocket;

import cn.gdeiassistant.common.pojo.entity.User;
import cn.gdeiassistant.common.redis.usercertificate.UserCertificateDao;
import cn.gdeiassistant.common.tools.utils.JwtUtil;
import cn.gdeiassistant.core.user.mapper.UserMapper;
import cn.gdeiassistant.core.user.pojo.entity.CampusAccountView;
import com.auth0.jwt.interfaces.Claim;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SocialRealtimeHandlerAuthTest {

    @Mock
    private JwtUtil jwtUtil;
    @Mock
    private UserCertificateDao userCertificateDao;
    @Mock
    private UserMapper userMapper;
    @Mock
    private SocialRealtimeHub realtimeHub;
    @Mock
    private WebSocketSession session;

    @InjectMocks
    private SocialRealtimeHandler handler;

    private final Map<String, Object> attrs = new ConcurrentHashMap<>();

    @BeforeEach
    void setUp() throws Exception {
        when(session.getAttributes()).thenReturn(attrs);
        doNothing().when(session).close(any(CloseStatus.class));
    }

    @AfterEach
    void tearDown() { handler.destroy(); }

    @Test
    void establishedConnectionClosesWhenItsJwtExpires() throws Exception {
        Claim sid = mock(Claim.class), username = mock(Claim.class), expiry = mock(Claim.class);
        when(sid.asString()).thenReturn("sid");
        when(username.asString()).thenReturn("alice");
        when(expiry.asLong()).thenReturn(java.time.Instant.now().getEpochSecond() + 60);
        when(jwtUtil.verifyAndParse("tok")).thenReturn(Map.of("sessionId", sid, "username", username, "exp", expiry));
        User login = new User(); login.setUsername("alice");
        when(userCertificateDao.queryUserLoginCertificate("sid")).thenReturn(login);
        CampusAccountView active = new CampusAccountView(); active.setId(1L); active.setStatus("ACTIVE");
        when(userMapper.selectUser("alice")).thenReturn(active);
        when(session.getId()).thenReturn("ws-1");
        when(session.isOpen()).thenReturn(true);
        handler.handleTextMessage(session, new TextMessage("{\"type\":\"auth\",\"token\":\"tok\"}"));
        verify(realtimeHub).register(1L, session);
        verify(session).sendMessage(argThat(message -> message.getPayload().toString().contains("ready")));
        attrs.put("expiresAt", java.time.Instant.now().getEpochSecond() - 1);
        handler.handleTextMessage(session, new TextMessage("{\"type\":\"ping\"}"));
        verify(session).close(CloseStatus.POLICY_VIOLATION);
        verify(realtimeHub).unregister(1L, session);
    }

    @Test
    void missingTokenClosesWithoutRegister() throws Exception {
        handler.handleTextMessage(session, new TextMessage("{\"type\":\"auth\"}"));
        verify(session).close(CloseStatus.POLICY_VIOLATION);
        verify(realtimeHub, never()).register(anyLong(), any());
    }

    @Test
    void invalidTokenCloses() throws Exception {
        when(jwtUtil.verifyAndParse("bad")).thenThrow(new RuntimeException("expired"));
        handler.handleTextMessage(session, new TextMessage("{\"type\":\"auth\",\"token\":\"bad\"}"));
        verify(session).close(CloseStatus.POLICY_VIOLATION);
        verify(realtimeHub, never()).register(anyLong(), any());
    }

    @Test
    void revokedSessionCloses() throws Exception {
        Claim sessionClaim = mock(Claim.class);
        Claim usernameClaim = mock(Claim.class);
        when(sessionClaim.asString()).thenReturn("sid");
        when(usernameClaim.asString()).thenReturn("alice");
        Map<String, Claim> claims = new HashMap<>();
        claims.put("sessionId", sessionClaim);
        claims.put("username", usernameClaim);
        when(jwtUtil.verifyAndParse("tok")).thenReturn(claims);
        when(userCertificateDao.queryUserLoginCertificate("sid")).thenReturn(null);

        handler.handleTextMessage(session, new TextMessage("{\"type\":\"auth\",\"token\":\"tok\"}"));
        verify(session).close(CloseStatus.POLICY_VIOLATION);
        verify(realtimeHub, never()).register(anyLong(), any());
    }

    @Test
    void inactiveUserCloses() throws Exception {
        Claim sessionClaim = mock(Claim.class);
        Claim usernameClaim = mock(Claim.class);
        when(sessionClaim.asString()).thenReturn("sid");
        when(usernameClaim.asString()).thenReturn("alice");
        Map<String, Claim> claims = new HashMap<>();
        claims.put("sessionId", sessionClaim);
        claims.put("username", usernameClaim);
        when(jwtUtil.verifyAndParse("tok")).thenReturn(claims);
        User login = new User();
        login.setUsername("alice");
        when(userCertificateDao.queryUserLoginCertificate("sid")).thenReturn(login);
        CampusAccountView closed = new CampusAccountView();
        closed.setId(1L);
        closed.setStatus("CLOSED");
        when(userMapper.selectUser("alice")).thenReturn(closed);

        handler.handleTextMessage(session, new TextMessage("{\"type\":\"auth\",\"token\":\"tok\"}"));
        verify(session).close(CloseStatus.POLICY_VIOLATION);
        verify(realtimeHub, never()).register(anyLong(), any());
    }
    @Test void authenticatedPingRechecksAccountAndStopsWhenCacheOrDatabaseFails() throws Exception {
        when(session.getId()).thenReturn("synthetic-socket");
        attrs.put("authenticated",true);attrs.put("userId",1L);attrs.put("username","alice");attrs.put("sessionId","sid");attrs.put("expiresAt",java.time.Instant.now().getEpochSecond()+60);
        when(userCertificateDao.queryUserLoginCertificate("sid")).thenReturn(new User("alice"));
        var active=new CampusAccountView();active.setId(1L);active.setStatus("ACTIVE");when(userMapper.selectUserById(1L)).thenReturn(active);
        handler.handleTextMessage(session,new TextMessage("{\"type\":\"ping\"}"));
        verify(session).sendMessage(argThat(m->m.getPayload().toString().contains("pong")));
        when(session.isOpen()).thenReturn(true);when(userMapper.selectUserById(1L)).thenThrow(new RuntimeException("synthetic db outage"));
        handler.handleTextMessage(session,new TextMessage("{\"type\":\"ping\"}"));verify(realtimeHub).unregister(1L,session);
        attrs.put("authenticated",true);when(userCertificateDao.queryUserLoginCertificate("sid")).thenThrow(new RuntimeException("synthetic cache outage"));
        handler.handleTextMessage(session,new TextMessage("{\"type\":\"ping\"}"));verify(realtimeHub,times(2)).unregister(1L,session);
        org.junit.jupiter.api.Assertions.assertEquals(false,attrs.get("authenticated"));
    }
    @Test void malformedAndUnauthenticatedFramesNeverProducePongAndCloseCancelsTimers() throws Exception {
        handler.handleTextMessage(session,new TextMessage("{broken"));verify(session).close(CloseStatus.BAD_DATA);
        handler.handleTextMessage(session,new TextMessage("{\"type\":\"ping\"}"));verify(session).close(CloseStatus.POLICY_VIOLATION);
        when(session.getId()).thenReturn("synthetic-socket");handler.afterConnectionEstablished(session);
        attrs.put("userId",Integer.valueOf(1));handler.afterConnectionClosed(session,CloseStatus.NORMAL);verify(realtimeHub).unregister(1L,session);
        verify(session,never()).sendMessage(any());
    }
    @Test void missingIdentityClaimsAndMissingExpiryFailAuthenticationBeforeRegistration() throws Exception {
        when(jwtUtil.verifyAndParse("empty")).thenReturn(Map.of());
        handler.handleTextMessage(session,new TextMessage("{\"type\":\"auth\",\"token\":\"empty\"}"));
        var sid=mock(Claim.class);var username=mock(Claim.class);when(sid.asString()).thenReturn("sid");when(username.asString()).thenReturn("alice");
        when(jwtUtil.verifyAndParse("no-expiry")).thenReturn(Map.of("sessionId",sid,"username",username));
        when(userCertificateDao.queryUserLoginCertificate("sid")).thenReturn(new User("alice"));
        var active=new CampusAccountView();active.setId(1L);active.setStatus("ACTIVE");when(userMapper.selectUser("alice")).thenReturn(active);
        handler.handleTextMessage(session,new TextMessage("{\"type\":\"auth\",\"token\":\"no-expiry\"}"));
        verify(realtimeHub,never()).register(anyLong(),any());verify(session,times(2)).close(CloseStatus.POLICY_VIOLATION);
    }

}
