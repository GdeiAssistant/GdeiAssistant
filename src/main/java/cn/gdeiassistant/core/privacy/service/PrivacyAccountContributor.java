package cn.gdeiassistant.core.privacy.service;

import cn.gdeiassistant.common.account.AccountClosure;
import cn.gdeiassistant.common.account.AccountLifecycleContributor;
import cn.gdeiassistant.core.privacy.mapper.PrivacyMapper;
import org.springframework.stereotype.Component;

/**
 * Privacy settings fall back to their defaults after closure.
 */
@Component
public class PrivacyAccountContributor implements AccountLifecycleContributor {

    private final PrivacyMapper privacyMapper;

    public PrivacyAccountContributor(PrivacyMapper privacyMapper) {
        this.privacyMapper = privacyMapper;
    }

    @Override
    public void purge(AccountClosure closure) {
        privacyMapper.resetPrivacy(closure.username());
    }
}
