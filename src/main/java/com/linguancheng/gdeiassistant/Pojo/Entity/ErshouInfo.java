package com.linguancheng.gdeiassistant.Pojo.Entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.Serializable;

@Component
@Scope("prototype")
@JsonIgnoreProperties(ignoreUnknown = true)
public class ErshouInfo implements Serializable {

    private AuthorProfile authorProfile;

    private ErshouItem ershouItem;

    public AuthorProfile getAuthorProfile() {
        return authorProfile;
    }

    public void setAuthorProfile(AuthorProfile authorProfile) {
        this.authorProfile = authorProfile;
    }

    public ErshouItem getErshouItem() {
        return ershouItem;
    }

    public void setErshouItem(ErshouItem ershouItem) {
        this.ershouItem = ershouItem;
    }
}
