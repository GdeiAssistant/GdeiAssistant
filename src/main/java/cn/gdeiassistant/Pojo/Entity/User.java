package cn.gdeiassistant.Pojo.Entity;

import cn.gdeiassistant.ValidGroup.User.UserLoginValidGroup;
import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.hibernate.validator.constraints.NotBlank;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.validation.constraints.Size;
import java.io.Serializable;

@Component
@Scope("prototype")
@JsonIgnoreProperties(ignoreUnknown = true)
public class User implements Serializable, Entity {

    @NotBlank(message = "用户名不能为空", groups = {UserLoginValidGroup.class})
    @Size(min = 1, max = 20, message = "用户名长度超过限制", groups = {UserLoginValidGroup.class})
    //校园网络账号用户名
    private String username;

    @NotBlank(message = "密码不能为空", groups = {UserLoginValidGroup.class})
    @Size(min = 1, max = 35, message = "密码长度超过限制", groups = {UserLoginValidGroup.class})
    @JSONField(serialize = false)
    //校园网络账号密码
    private String password;

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public User() {
        super();
    }

    public User(String username) {
        this.username = username;
    }

    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        User user = (User) o;

        if (username != null ? !username.equals(user.username) : user.username != null) return false;
        return password != null ? password.equals(user.password) : user.password == null;
    }

    @Override
    public int hashCode() {
        int result = username != null ? username.hashCode() : 0;
        result = 31 * result + (password != null ? password.hashCode() : 0);
        return result;
    }
}
