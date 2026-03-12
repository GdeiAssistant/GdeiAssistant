package cn.gdeiassistant.core.message.service.provider;

import java.util.List;

public interface InteractionMessageProvider {

    List<InteractionMessageRecord> queryMessages(String username, int limit);
}
