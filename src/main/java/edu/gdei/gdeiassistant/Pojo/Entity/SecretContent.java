package edu.gdei.gdeiassistant.Pojo.Entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
@JsonIgnoreProperties(ignoreUnknown = true)
public class SecretContent extends Secret {

    public SecretContent() {
        super();
    }

    public SecretContent(Secret secret, String username) {
        this.setContent(secret.getContent());
        this.setTheme(secret.getTheme());
        this.setType(secret.getType());
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
