package cn.gdeiassistant.core.lostandfound.pojo.entity;

import cn.gdeiassistant.core.profile.pojo.entity.ProfileEntity;

import java.io.Serializable;

/**
 * Mapper selectInfoByID 结果封装：物品实体 + 发布者资料。
 */
public class LostAndFoundDetailEntity implements Serializable {

    private LostAndFoundItemEntity item;
    private ProfileEntity profile;

    public LostAndFoundItemEntity getItem() { return item; }
    public void setItem(LostAndFoundItemEntity item) { this.item = item; }
    public ProfileEntity getProfile() { return profile; }
    public void setProfile(ProfileEntity profile) { this.profile = profile; }
}
