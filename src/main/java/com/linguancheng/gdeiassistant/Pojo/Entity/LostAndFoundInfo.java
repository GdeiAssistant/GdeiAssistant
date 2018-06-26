package com.linguancheng.gdeiassistant.Pojo.Entity;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.Serializable;

@Component
@Scope("prototype")
public class LostAndFoundInfo implements Serializable {

    private LostAndFoundItem lostAndFoundItem;

    private AuthorProfile authorProfile;

    public LostAndFoundItem getLostAndFoundItem() {
        return lostAndFoundItem;
    }

    public void setLostAndFoundItem(LostAndFoundItem lostAndFoundItem) {
        this.lostAndFoundItem = lostAndFoundItem;
    }

    public AuthorProfile getAuthorProfile() {
        return authorProfile;
    }

    public void setAuthorProfile(AuthorProfile authorProfile) {
        this.authorProfile = authorProfile;
    }
}
