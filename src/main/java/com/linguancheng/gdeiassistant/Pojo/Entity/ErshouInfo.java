package com.linguancheng.gdeiassistant.Pojo.Entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.Serializable;

@Component
@Scope("prototype")
@JsonIgnoreProperties(ignoreUnknown = true)
public class ErshouInfo implements Serializable {

    private Profile profile;

    private ErshouItem ershouItem;

    public Profile getProfile() {
        return profile;
    }

    public void setProfile(Profile profile) {
        this.profile = profile;
    }

    public ErshouItem getErshouItem() {
        return ershouItem;
    }

    public void setErshouItem(ErshouItem ershouItem) {
        this.ershouItem = ershouItem;
    }
}
