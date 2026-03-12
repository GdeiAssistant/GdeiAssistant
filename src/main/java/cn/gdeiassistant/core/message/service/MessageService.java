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
        return interactionNotificationService.queryInteractionMessages(sessionId, start, size);
    }

    public Integer queryInteractionUnreadCount(String sessionId) {
        return interactionNotificationService.queryInteractionUnreadCount(sessionId);
    }

    public void markInteractionMessageRead(String sessionId, String id) {
        interactionNotificationService.markInteractionNotificationRead(sessionId, id);
    }

    public void markAllInteractionMessagesRead(String sessionId) {
        interactionNotificationService.markAllInteractionNotificationsRead(sessionId);
    }
}
