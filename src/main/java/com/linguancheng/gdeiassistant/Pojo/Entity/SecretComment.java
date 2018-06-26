package com.linguancheng.gdeiassistant.Pojo.Entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.validation.constraints.Min;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

@Component
@Scope("prototype")
@JsonIgnoreProperties(ignoreUnknown = true)
public class SecretComment implements Serializable {

    private int id;

    private String username;

    private String comment;

    private Date publishTime;

    private int avatarTheme;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getPublishTime() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(publishTime);
    }

    public void setPublishTime(String publishTime) throws ParseException {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        this.publishTime = simpleDateFormat.parse(publishTime);
    }

    public int getAvatarTheme() {
        return avatarTheme;
    }

    public void setAvatarTheme(int avatarTheme) {
        this.avatarTheme = avatarTheme;
    }
}
