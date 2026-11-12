package cn.gdeiassistant.core.phone.service;

import cn.gdeiassistant.common.account.AccountClosure;
import cn.gdeiassistant.common.account.AccountLifecycleContributor;
import cn.gdeiassistant.core.phone.mapper.PhoneMapper;
import org.springframework.stereotype.Component;

/**
 * The bound phone number is unbound together with the account.
 */
@Component
public class PhoneAccountContributor implements AccountLifecycleContributor {

    private final PhoneMapper phoneMapper;

    public PhoneAccountContributor(PhoneMapper phoneMapper) {
        this.phoneMapper = phoneMapper;
    }

    @Override
    public void purge(AccountClosure closure) {
        if (phoneMapper.selectPhone(closure.username()) != null) {
            phoneMapper.deletePhone(closure.username());
        }
    }
}
