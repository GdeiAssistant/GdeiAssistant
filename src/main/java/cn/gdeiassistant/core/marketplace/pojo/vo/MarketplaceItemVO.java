package cn.gdeiassistant.core.marketplace.pojo.vo;

import cn.gdeiassistant.core.profile.pojo.vo.ProfileVO;
import cn.gdeiassistant.core.marketplace.pojo.entity.MarketplaceItemEntity;

import java.io.Serializable;

/**
 * 二手商品详情视图，返回给前端的展示数据。
 */
public class MarketplaceItemVO implements Serializable {

    private ProfileVO profile;
    private MarketplaceItemEntity marketplaceItem;

    public ProfileVO getProfile() { return profile; }
    public void setProfile(ProfileVO profile) { this.profile = profile; }
    public MarketplaceItemEntity getSecondhandItem() { return marketplaceItem; }
    public void setSecondhandItem(MarketplaceItemEntity marketplaceItem) { this.marketplaceItem = marketplaceItem; }
}
