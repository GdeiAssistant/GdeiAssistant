package cn.gdeiassistant.core.dating.service;

import cn.gdeiassistant.common.account.AccountClosure;
import cn.gdeiassistant.common.account.AccountLifecycleContributor;
import cn.gdeiassistant.core.dating.mapper.DatingMapper;
import org.springframework.stereotype.Component;

/**
 * Dating profiles are hidden after closure; historical rows are kept.
 */
@Component
public class DatingAccountContributor implements AccountLifecycleContributor {

    private final DatingMapper datingMapper;

    public DatingAccountContributor(DatingMapper datingMapper) {
        this.datingMapper = datingMapper;
    }

    @Override
    public void purge(AccountClosure closure) {
        datingMapper.hideDatingProfilesByUsername(closure.username());
    }
}
