package cn.gdeiassistant.core.social.websocket;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.socket.WebSocketSession;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SocialRealtimeHubConcurrencyTest {
    private WebSocketSession session() {
        var session=mock(WebSocketSession.class);
        when(session.isOpen()).thenReturn(true);
        when(session.getAttributes()).thenReturn(Map.of("authenticated",true));
        return session;
    }
    @SuppressWarnings("unchecked")
    private void install(SocialRealtimeHub hub,Set<WebSocketSession> sessions) {
        var map=(ConcurrentHashMap<Long,Set<WebSocketSession>>)ReflectionTestUtils.getField(hub,"userSessions");
        map.put(1L,sessions);
    }
    private static void await(CountDownLatch latch) {
        try {if(!latch.await(5,TimeUnit.SECONDS))throw new AssertionError("synthetic barrier timeout");}
        catch(InterruptedException e){Thread.currentThread().interrupt();throw new AssertionError(e);}
    }
    @Test void lastDisconnectCannotEraseAConcurrentNewConnection() throws Exception {
        var checked=new CountDownLatch(1);var resume=new CountDownLatch(1);
        var sessions=new CopyOnWriteArraySet<WebSocketSession>() {
            @Override public boolean isEmpty(){boolean empty=super.isEmpty();checked.countDown();await(resume);return empty;}
        };
        var old=session();var fresh=session();sessions.add(old);
        var hub=new SocialRealtimeHub();install(hub,sessions);var workers=Executors.newFixedThreadPool(2);
        try {
            var removal=workers.submit(()->hub.unregister(1L,old));await(checked);
            var started=new CountDownLatch(1);
            var registration=workers.submit(()->{started.countDown();hub.register(1L,fresh);});await(started);
            try {assertThrows(TimeoutException.class,()->registration.get(250,TimeUnit.MILLISECONDS));}
            finally {resume.countDown();}
            removal.get(5,TimeUnit.SECONDS);registration.get(5,TimeUnit.SECONDS);
            hub.pushToUser(1L,Map.of("type","synthetic"));verify(fresh).sendMessage(any());verify(old,never()).sendMessage(any());
        } finally {resume.countDown();workers.shutdownNow();assertTrue(workers.awaitTermination(5,TimeUnit.SECONDS));}
    }
    @Test void userDisconnectWaitsForRegistrationAndClosesThatConnection() throws Exception {
        var adding=new CountDownLatch(1);var resume=new CountDownLatch(1);var fresh=session();
        var sessions=new CopyOnWriteArraySet<WebSocketSession>() {
            @Override public boolean add(WebSocketSession value){if(value==fresh){adding.countDown();await(resume);}return super.add(value);}
        };
        var old=session();sessions.add(old);var hub=new SocialRealtimeHub();install(hub,sessions);
        var workers=Executors.newFixedThreadPool(2);
        try {
            var registration=workers.submit(()->hub.register(1L,fresh));await(adding);
            var started=new CountDownLatch(1);
            var disconnect=workers.submit(()->{started.countDown();hub.disconnectUser(1L);});await(started);
            try {assertThrows(TimeoutException.class,()->disconnect.get(250,TimeUnit.MILLISECONDS));}
            finally {resume.countDown();}
            registration.get(5,TimeUnit.SECONDS);disconnect.get(5,TimeUnit.SECONDS);
            verify(old).close();verify(fresh).close();
            hub.pushToUser(1L,Map.of("type","synthetic"));verify(fresh,never()).sendMessage(any());
        } finally {resume.countDown();workers.shutdownNow();assertTrue(workers.awaitTermination(5,TimeUnit.SECONDS));}
    }
}
