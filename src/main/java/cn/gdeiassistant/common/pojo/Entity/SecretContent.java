package cn.gdeiassistant.common.pojo.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SecretContent extends Secret {

    public SecretContent() {
        super();
    }

    public SecretContent(Secret secret, String username) {
        this.setContent(secret.getContent());
        this.setTheme(secret.getTheme());
        this.setType(secret.getType());
        this.setTimer(secret.getTimer());
        this.setUsername(username);
    }

    private String username;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
