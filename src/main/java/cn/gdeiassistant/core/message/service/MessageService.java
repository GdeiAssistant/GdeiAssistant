package cn.gdeiassistant.core.message.service;

import cn.gdeiassistant.core.message.pojo.vo.InteractionMessageVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MessageService {

    @Autowired
    private InteractionNotificationService interactionNotificationService;

    public List<InteractionMessageVO> queryInteractionMessages(String sessionId, Integer start, Integer size) {
        return queryInteractionMessages(sessionId, start, size, false);
    }

    public List<InteractionMessageVO> queryInteractionMessages(String sessionId, Integer start, Integer size,
            boolean includeLegacyDating) {
        return interactionNotificationService.queryInteractionMessages(sessionId, start, size, includeLegacyDating);
    }

    public Integer queryInteractionUnreadCount(String sessionId) {
        return queryInteractionUnreadCount(sessionId, false);
    }

    public Integer queryInteractionUnreadCount(String sessionId, boolean includeLegacyDating) {
        return interactionNotificationService.queryInteractionUnreadCount(sessionId, includeLegacyDating);
    }

    public void markInteractionMessageRead(String sessionId, String id) {
        interactionNotificationService.markInteractionNotificationRead(sessionId, id);
    }

    public void markAllInteractionMessagesRead(String sessionId) {
        markAllInteractionMessagesRead(sessionId, false);
    }

    public void markAllInteractionMessagesRead(String sessionId, boolean includeLegacyDating) {
        interactionNotificationService.markAllInteractionNotificationsRead(sessionId, includeLegacyDating);
    }
}
