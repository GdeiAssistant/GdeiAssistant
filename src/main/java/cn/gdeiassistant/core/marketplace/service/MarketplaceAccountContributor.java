package cn.gdeiassistant.core.marketplace.service;

import cn.gdeiassistant.common.account.AccountCloseBlocker;
import cn.gdeiassistant.common.account.AccountLifecycleContributor;
import cn.gdeiassistant.core.marketplace.mapper.MarketplaceMapper;
import cn.gdeiassistant.core.marketplace.pojo.entity.MarketplaceItemEntity;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import cn.gdeiassistant.common.account.AccountDataExport;
import cn.gdeiassistant.common.constant.ItemConstantUtils;

/**
 * Second-hand marketplace: items still on sale block closure.
 */
@Component
public class MarketplaceAccountContributor implements AccountLifecycleContributor {

    /** Index into ERSHOU_STATE_TYPE: on sale. */
    private static final int ON_SALE = 1;

    private final MarketplaceMapper marketplaceMapper;

    public MarketplaceAccountContributor(MarketplaceMapper marketplaceMapper) {
        this.marketplaceMapper = marketplaceMapper;
    }

    @Override
    public List<AccountCloseBlocker> blockers(String username) {
        List<AccountCloseBlocker> blockers = new ArrayList<>();
        for (MarketplaceItemEntity item : marketplaceMapper.selectOwnerItemStates(username)) {
            if (item.getState() != null && item.getState() == ON_SALE) {
                blockers.add(new AccountCloseBlocker(
                        "二手交易平台存在未交易完成的物品", "请下架或确认出售账号下的所有二手交易物品"));
            }
        }
        return blockers;
    }

    @Override
    public void decorateExport(AccountDataExport export) {
        export.relabel("ershouItems", "type", ItemConstantUtils.ERSHOU_ITEM_TYPE);
        export.relabel("ershouItems", "state", ItemConstantUtils.ERSHOU_STATE_TYPE);
    }
}
