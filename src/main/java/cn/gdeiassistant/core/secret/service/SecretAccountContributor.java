package cn.gdeiassistant.core.secret.service;

import cn.gdeiassistant.common.account.AccountClosure;
import cn.gdeiassistant.common.account.AccountLifecycleContributor;
import cn.gdeiassistant.core.secret.mapper.SecretMapper;
import org.springframework.stereotype.Component;
import cn.gdeiassistant.common.account.AccountDataExport;
import cn.gdeiassistant.common.constant.ItemConstantUtils;

/**
 * Tree-hole posts are anonymized after closure; the content itself stays.
 */
@Component
public class SecretAccountContributor implements AccountLifecycleContributor {

    private final SecretMapper secretMapper;

    public SecretAccountContributor(SecretMapper secretMapper) {
        this.secretMapper = secretMapper;
    }

    @Override
    public void purge(AccountClosure closure) {
        secretMapper.anonymizeUsername(closure.username(), closure.anonymizedName());
    }

    @Override
    public void decorateExport(AccountDataExport export) {
        export.relabel("secretItems", "state", ItemConstantUtils.SECRET_STATE_TYPE);
        export.relabel("secretItems", "type", ItemConstantUtils.SECRET_ITEM_TYPE);
    }
}
