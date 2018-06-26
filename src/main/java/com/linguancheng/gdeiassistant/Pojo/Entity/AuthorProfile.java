package com.linguancheng.gdeiassistant.Pojo.Entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.Serializable;

@Component
@Scope("prototype")
@JsonIgnoreProperties(ignoreUnknown = true)
public class AuthorProfile implements Serializable {

    public AuthorProfile() {
        super();
    }

    public AuthorProfile(String username, String kickname, String avatarURL) {
        this.username = username;
        this.kickname = kickname;
        this.avatarURL = avatarURL;
    }

    private String username;

    private String kickname;

    private String avatarURL;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getKickname() {
        return kickname;
    }

    public void setKickname(String kickname) {
        this.kickname = kickname;
    }

    public String getAvatarURL() {
        return avatarURL;
    }

    public void setAvatarURL(String avatarURL) {
        this.avatarURL = avatarURL;
    }
}
