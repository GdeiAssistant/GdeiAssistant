package cn.gdeiassistant.core.cet.service;

import cn.gdeiassistant.common.account.AccountClosure;
import cn.gdeiassistant.common.account.AccountLifecycleContributor;
import cn.gdeiassistant.core.cet.mapper.CetMapper;
import org.springframework.stereotype.Component;

/**
 * The stored CET admission ticket number goes with the account.
 */
@Component
public class CetAccountContributor implements AccountLifecycleContributor {

    private final CetMapper cetMapper;

    public CetAccountContributor(CetMapper cetMapper) {
        this.cetMapper = cetMapper;
    }

    @Override
    public void purge(AccountClosure closure) {
        if (cetMapper.selectNumber(closure.username()) != null) {
            cetMapper.updateNumber(closure.username(), null);
        }
    }
}
