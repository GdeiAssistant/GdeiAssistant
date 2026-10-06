package cn.gdeiassistant.common.pojo.entity;


import java.io.Serializable;

public class LostAndFoundInfo implements Serializable, Entity {

    private LostAndFoundItem lostAndFoundItem;

    private Profile profile;

    public LostAndFoundItem getLostAndFoundItem() {
        return lostAndFoundItem;
    }

    public void setLostAndFoundItem(LostAndFoundItem lostAndFoundItem) {
        this.lostAndFoundItem = lostAndFoundItem;
    }

    public Profile getProfile() {
        return profile;
    }

    public void setProfile(Profile profile) {
        this.profile = profile;
    }
}
