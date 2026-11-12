package cn.gdeiassistant.core.message.service;

import cn.gdeiassistant.common.account.AccountClosure;
import cn.gdeiassistant.common.account.AccountLifecycleContributor;
import cn.gdeiassistant.core.message.mapper.InteractionNotificationMapper;
import org.springframework.stereotype.Component;

/**
 * The actor in interaction notifications is anonymized; recipients keep their copy.
 */
@Component
public class MessageAccountContributor implements AccountLifecycleContributor {

    private static final String ANONYMIZED_ACTOR_NAME = "已注销用户";

    private final InteractionNotificationMapper interactionNotificationMapper;

    public MessageAccountContributor(InteractionNotificationMapper interactionNotificationMapper) {
        this.interactionNotificationMapper = interactionNotificationMapper;
    }

    @Override
    public void purge(AccountClosure closure) {
        interactionNotificationMapper.anonymizeActorUsername(
                closure.username(), closure.anonymizedName(), ANONYMIZED_ACTOR_NAME);
    }
}
