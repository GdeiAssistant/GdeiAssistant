package edu.gdei.gdeiassistant.Pojo.Entity;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.Serializable;

@Component
@Scope("prototype")
public class LostAndFoundInfo implements Serializable {

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
