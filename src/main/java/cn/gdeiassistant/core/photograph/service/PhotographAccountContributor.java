package cn.gdeiassistant.core.photograph.service;

import cn.gdeiassistant.common.account.AccountClosure;
import cn.gdeiassistant.common.account.AccountLifecycleContributor;
import cn.gdeiassistant.core.photograph.mapper.PhotographMapper;
import org.springframework.stereotype.Component;
import cn.gdeiassistant.common.account.AccountDataExport;
import cn.gdeiassistant.common.constant.ItemConstantUtils;

/**
 * Campus photos and comments are anonymized after closure; the content itself stays.
 */
@Component
public class PhotographAccountContributor implements AccountLifecycleContributor {

    private final PhotographMapper photographMapper;

    public PhotographAccountContributor(PhotographMapper photographMapper) {
        this.photographMapper = photographMapper;
    }

    @Override
    public void purge(AccountClosure closure) {
        photographMapper.anonymizeUsername(closure.username(), closure.anonymizedName());
    }

    @Override
    public void decorateExport(AccountDataExport export) {
        export.relabel("photographItems", "type", ItemConstantUtils.PHOTOGRAPH_ITEM_TYPE);
    }
}
