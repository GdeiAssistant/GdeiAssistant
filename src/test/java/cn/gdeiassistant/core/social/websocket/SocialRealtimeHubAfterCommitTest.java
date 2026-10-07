package cn.gdeiassistant.core.social.websocket;

import org.junit.jupiter.api.Test;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import java.io.IOException;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

class SocialRealtimeHubAfterCommitTest {
    private WebSocketSession session(boolean authenticated) {
        var session=mock(WebSocketSession.class);
        when(session.isOpen()).thenReturn(true);
        when(session.getAttributes()).thenReturn(Map.of("authenticated",authenticated,"userId",1L));
        return session;
    }
    @Test void rollbackSendsNothingAndCommitTargetsOnlyAuthenticatedOwnerDevices() throws Exception {
        var hub=new SocialRealtimeHub();var owner=session(true);var pending=session(false);var other=session(true);
        hub.register(1L,owner);hub.register(1L,pending);hub.register(2L,other);
        TransactionSynchronizationManager.initSynchronization();
        try {
            hub.pushToUserAfterCommit(1L,Map.of("type","message.created","seq","3"));
            var callbacks=TransactionSynchronizationManager.getSynchronizations();assertEquals(1,callbacks.size());
            callbacks.get(0).afterCompletion(TransactionSynchronization.STATUS_ROLLED_BACK);
            verify(owner,never()).sendMessage(any());
            callbacks.get(0).afterCommit();
            verify(owner).sendMessage(argThat(m->((TextMessage)m).getPayload().contains("\"seq\":\"3\"")));
            verify(pending,never()).sendMessage(any());verify(other,never()).sendMessage(any());
        } finally {TransactionSynchronizationManager.clearSynchronization();}
    }
    @Test void brokenPeerDoesNotInterruptDeliveryAndDisconnectRemovesRegistrations() throws Exception {
        var hub=new SocialRealtimeHub();var broken=session(true);var working=session(true);
        doThrow(new IOException("synthetic disconnected transport")).when(broken).sendMessage(any());
        doThrow(new IOException("synthetic close failure")).when(broken).close();
        hub.register(1L,broken);hub.register(1L,working);
        hub.pushToUserAfterCommit(1L,Map.of("type","social.changed"));verify(working).sendMessage(any());
        hub.disconnectSession(working);verify(working).close();
        hub.disconnectUser(1L);verify(broken).close();
        hub.pushToUser(1L,Map.of("type","social.changed"));verify(working,times(1)).sendMessage(any());
        hub.unregister(999L,working);hub.disconnectUser(999L);
    }
}
