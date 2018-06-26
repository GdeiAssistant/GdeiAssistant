package com.linguancheng.gdeiassistant.Pojo.Entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.Serializable;

@Component
@Scope("prototype")
@JsonIgnoreProperties(ignoreUnknown = true)
public class Introduction implements Serializable {

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
