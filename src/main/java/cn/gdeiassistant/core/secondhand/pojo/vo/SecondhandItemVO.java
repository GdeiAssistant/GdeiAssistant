package cn.gdeiassistant.core.secondhand.pojo.vo;

import cn.gdeiassistant.core.profile.pojo.vo.ProfileVO;
import cn.gdeiassistant.core.secondhand.pojo.entity.SecondhandItemEntity;

import java.io.Serializable;

/**
 * 二手商品详情视图，返回给前端的展示数据。
 */
public class SecondhandItemVO implements Serializable {

    private ProfileVO profile;
    private SecondhandItemEntity secondhandItem;

    public ProfileVO getProfile() { return profile; }
    public void setProfile(ProfileVO profile) { this.profile = profile; }
    public SecondhandItemEntity getSecondhandItem() { return secondhandItem; }
    public void setSecondhandItem(SecondhandItemEntity secondhandItem) { this.secondhandItem = secondhandItem; }
}
