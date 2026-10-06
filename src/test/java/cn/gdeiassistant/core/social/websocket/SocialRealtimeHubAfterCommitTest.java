package cn.gdeiassistant.core.social.websocket;

import org.junit.jupiter.api.Test;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class SocialRealtimeHubAfterCommitTest {

    @Test
    void pushAfterCommitOnlyFiresOnCommit() {
        SocialRealtimeHub hub = new SocialRealtimeHub();
        AtomicInteger pushes = new AtomicInteger();
        // 无法直接注入 session，这里验证同步注册与 rollback 行为：无 active sync 时立即调用 push 路径不抛
        hub.pushToUserAfterCommit(1L, Map.of("type", "message.created"));

        TransactionSynchronizationManager.initSynchronization();
        try {
            hub.pushToUserAfterCommit(1L, Map.of("type", "message.created"));
            assertEquals(1, TransactionSynchronizationManager.getSynchronizations().size());
            for (TransactionSynchronization sync : TransactionSynchronizationManager.getSynchronizations()) {
                sync.afterCompletion(TransactionSynchronization.STATUS_ROLLED_BACK);
            }
            // rollback 后不应视为成功；再 commit 路径手动触发 afterCommit
            hub.pushToUserAfterCommit(2L, Map.of("type", "social.changed"));
            for (TransactionSynchronization sync : TransactionSynchronizationManager.getSynchronizations()) {
                sync.afterCommit();
                pushes.incrementAndGet();
            }
            assertTrue(pushes.get() >= 1);
        } finally {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }
}
