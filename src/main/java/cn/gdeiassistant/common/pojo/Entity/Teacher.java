package cn.gdeiassistant.common.pojo.Entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotBlank;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import jakarta.validation.constraints.Size;
import java.io.Serializable;

@Component
@Scope("prototype")
@JsonIgnoreProperties(ignoreUnknown = true)
public class Teacher implements Serializable, Entity{

    @NotBlank(message = "用户名不能为空")
    @Size(min = 1, max = 20, message = "用户名长度超过限制")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Size(min = 1, max = 35, message = "密码长度超过限制")
    private String password;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
