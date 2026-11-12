package cn.gdeiassistant.core.topic.service;

import cn.gdeiassistant.common.account.AccountClosure;
import cn.gdeiassistant.common.account.AccountLifecycleContributor;
import cn.gdeiassistant.core.topic.mapper.TopicMapper;
import org.springframework.stereotype.Component;

/**
 * Campus topic posts are anonymized after closure; the topics themselves stay.
 */
@Component
public class TopicAccountContributor implements AccountLifecycleContributor {

    private final TopicMapper topicMapper;

    public TopicAccountContributor(TopicMapper topicMapper) {
        this.topicMapper = topicMapper;
    }

    @Override
    public void purge(AccountClosure closure) {
        topicMapper.anonymizeUsername(closure.username(), closure.anonymizedName());
    }
}
