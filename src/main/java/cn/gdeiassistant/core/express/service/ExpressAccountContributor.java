package cn.gdeiassistant.core.express.service;

import cn.gdeiassistant.common.account.AccountClosure;
import cn.gdeiassistant.common.account.AccountLifecycleContributor;
import cn.gdeiassistant.core.express.mapper.ExpressMapper;
import org.springframework.stereotype.Component;
import cn.gdeiassistant.common.account.AccountDataExport;
import cn.gdeiassistant.common.constant.ItemConstantUtils;

/**
 * Confession-wall posts and comments are anonymized after closure.
 */
@Component
public class ExpressAccountContributor implements AccountLifecycleContributor {

    private final ExpressMapper expressMapper;

    public ExpressAccountContributor(ExpressMapper expressMapper) {
        this.expressMapper = expressMapper;
    }

    @Override
    public void purge(AccountClosure closure) {
        expressMapper.anonymizeByUsername(closure.username(), closure.anonymizedName());
        expressMapper.anonymizeCommentsByUsername(closure.username(), closure.anonymizedName());
    }

    @Override
    public void decorateExport(AccountDataExport export) {
        export.relabel("expressItems", "selfGender", ItemConstantUtils.EXPRESS_ITEM_GENDER_TYPE);
        export.relabel("expressItems", "personGender", ItemConstantUtils.EXPRESS_ITEM_GENDER_TYPE);
    }
}
