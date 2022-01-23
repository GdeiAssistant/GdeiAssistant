package cn.gdeiassistant.Pojo.Entity;

import cn.gdeiassistant.ValidGroup.User.UserLoginValidGroup;
import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import cn.gdeiassistant.Tools.Utils.StringEncryptUtils;
import com.taobao.wsgsvr.WsgException;
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



    public User decryptUser() {
        User decryptUser = new User();
        try {
            if (username != null) {
                decryptUser.setUsername(StringEncryptUtils.decryptString(username));
            }
            if (password != null) {
                decryptUser.setPassword(StringEncryptUtils.decryptString(password));
            }
        } catch (WsgException e) {
            e.printStackTrace();
        }
        return decryptUser;
    }

    public User encryptUser() {
        User encryptUser = new User();
        try {
            if (username != null) {
                encryptUser.setUsername(StringEncryptUtils.encryptString(username));
            }
            if (password != null) {
                encryptUser.setPassword(StringEncryptUtils.encryptString(password));
            }
        } catch (WsgException e) {
            e.printStackTrace();
        }
        return encryptUser;
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
}
