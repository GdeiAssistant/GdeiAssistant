package cn.gdeiassistant.core.social.service;

import cn.gdeiassistant.common.account.AccountClosure;
import cn.gdeiassistant.common.account.AccountLifecycleContributor;
import cn.gdeiassistant.core.social.mapper.SocialRelationMapper;
import cn.gdeiassistant.core.social.websocket.SocialRealtimeHub;
import org.springframework.stereotype.Component;

/**
 * Follow and block relations go during closure; live connections are dropped after commit.
 */
@Component
public class SocialAccountContributor implements AccountLifecycleContributor {

    private final SocialRelationMapper socialRelationMapper;
    private final SocialRealtimeHub realtimeHub;

    public SocialAccountContributor(SocialRelationMapper socialRelationMapper, SocialRealtimeHub realtimeHub) {
        this.socialRelationMapper = socialRelationMapper;
        this.realtimeHub = realtimeHub;
    }

    @Override
    public void purge(AccountClosure closure) {
        if (closure.userId() == null) {
            return;
        }
        socialRelationMapper.deleteAllFollowsForUser(closure.userId());
        socialRelationMapper.deleteAllBlocksForUser(closure.userId());
    }

    @Override
    public void cleanupAfterCommit(AccountClosure closure) {
        if (closure.userId() == null) {
            return;
        }
        realtimeHub.disconnectUser(closure.userId());
    }
}
