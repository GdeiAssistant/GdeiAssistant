package edu.gdei.gdeiassistant.Pojo.Entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
@JsonIgnoreProperties(ignoreUnknown = true)
public class CloseLog extends Log {

    private String username;

    private String resetname;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getResetname() {
        return resetname;
    }

    public void setResetname(String resetname) {
        this.resetname = resetname;
    }
}
