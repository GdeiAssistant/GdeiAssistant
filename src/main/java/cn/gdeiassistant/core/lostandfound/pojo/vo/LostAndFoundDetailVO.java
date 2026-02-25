package cn.gdeiassistant.core.lostandfound.pojo.vo;

import cn.gdeiassistant.core.profile.pojo.vo.ProfileVO;

import java.io.Serializable;

/**
 * 失物招领详情视图，返回给前端（物品 + 发布者资料）。
 */
public class LostAndFoundDetailVO implements Serializable {

    private LostAndFoundItemVO item;
    private ProfileVO profile;

    public LostAndFoundItemVO getItem() { return item; }
    public void setItem(LostAndFoundItemVO item) { this.item = item; }
    public ProfileVO getProfile() { return profile; }
    public void setProfile(ProfileVO profile) { this.profile = profile; }
}
