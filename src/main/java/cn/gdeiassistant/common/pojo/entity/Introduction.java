package cn.gdeiassistant.common.pojo.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Introduction implements Serializable, Entity {

    private String introductionContent;

    private String username;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getIntroductionContent() {
        return introductionContent;
    }

    public void setIntroductionContent(String introductionContent) {
        this.introductionContent = introductionContent;
    }
}
