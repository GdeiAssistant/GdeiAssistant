package cn.gdeiassistant.core.lostandfound.service;

import cn.gdeiassistant.common.account.AccountCloseBlocker;
import cn.gdeiassistant.common.account.AccountLifecycleContributor;
import cn.gdeiassistant.core.lostandfound.mapper.LostAndFoundMapper;
import cn.gdeiassistant.core.lostandfound.pojo.entity.LostAndFoundItemEntity;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import cn.gdeiassistant.common.account.AccountDataExport;
import cn.gdeiassistant.common.constant.ItemConstantUtils;

/**
 * Lost and found: items still open block closure.
 */
@Component
public class LostAndFoundAccountContributor implements AccountLifecycleContributor {

    /** Index into LOST_AND_FOUND_STATE_TYPE: still open. */
    private static final int OPEN = 0;

    private final LostAndFoundMapper lostAndFoundMapper;

    public LostAndFoundAccountContributor(LostAndFoundMapper lostAndFoundMapper) {
        this.lostAndFoundMapper = lostAndFoundMapper;
    }

    @Override
    public List<AccountCloseBlocker> blockers(String username) {
        List<AccountCloseBlocker> blockers = new ArrayList<>();
        for (LostAndFoundItemEntity item : lostAndFoundMapper.selectOwnerItemStates(username)) {
            if (item.getState() != null && item.getState() == OPEN) {
                blockers.add(new AccountCloseBlocker(
                        "失物招领平台存在未确认状态的物品", "请确认寻回账号下的所有失物招领物品"));
            }
        }
        return blockers;
    }

    @Override
    public void decorateExport(AccountDataExport export) {
        export.relabel("lostandfoundItems", "itemType", ItemConstantUtils.LOST_AND_FOUND_ITEM_TYPE);
        export.relabel("lostandfoundItems", "lostType", ItemConstantUtils.LOST_AND_FOUND_LOST_TYPE);
        export.relabel("lostandfoundItems", "state", ItemConstantUtils.LOST_AND_FOUND_STATE_TYPE);
    }
}
