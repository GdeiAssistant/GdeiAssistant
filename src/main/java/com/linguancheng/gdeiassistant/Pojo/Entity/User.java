package com.linguancheng.gdeiassistant.Pojo.Entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.linguancheng.gdeiassistant.Tools.StringEncryptUtils;
import com.linguancheng.gdeiassistant.ValidGroup.User.UserLoginValidGroup;
import com.taobao.wsgsvr.WsgException;
import org.hibernate.validator.constraints.NotBlank;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 * Created by linguancheng on 2017/7/16.
 */

@Component
@Scope("prototype")
@JsonIgnoreProperties(ignoreUnknown = true)
public class User implements Serializable {

    @NotBlank(message = "用户名不能为空", groups = {UserLoginValidGroup.class})
    @Size(min = 1, max = 20, message = "用户名长度超过限制", groups = {UserLoginValidGroup.class})
    //教务系统账号用户名
    private String username;

    @NotBlank(message = "密码不能为空", groups = {UserLoginValidGroup.class})
    @Size(min = 1, max = 35, message = "密码长度超过限制", groups = {UserLoginValidGroup.class})
    //教务系统账号密码
    private String password;

    //教务系统账号加密值
    private String keycode;

    //教务系统账号学号
    private String number;

    /**
     * 账号状态
     * 0为正常，-1为用户自主注销
     */
    private Integer state;

    /**
     * 用户所属的角色组
     */
    private Integer group;

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

    public String getKeycode() {
        return keycode;
    }

    public void setKeycode(String keycode) {
        this.keycode = keycode;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
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
            if (keycode != null) {
                decryptUser.setKeycode(StringEncryptUtils.decryptString(keycode));
            }
            if (number != null) {
                decryptUser.setNumber(StringEncryptUtils.decryptString(number));
            }
            if (state != null) {
                decryptUser.setState(state);
            }
            if (group != null) {
                decryptUser.setGroup(group);
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
            if (keycode != null) {
                encryptUser.setKeycode(StringEncryptUtils.encryptString(keycode));
            }
            if (number != null) {
                encryptUser.setNumber(StringEncryptUtils.encryptString(number));
            }
            if (state != null) {
                encryptUser.setState(state);
            }
            if (group != null) {
                encryptUser.setGroup(group);
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

    public User(String username, String password, String keycode, String number) {
        this.username = username;
        this.password = password;
        this.keycode = keycode;
        this.number = number;
    }

    public Integer getState() {
        return state;
    }

    public void setState(Integer state) {
        this.state = state;
    }

    public Integer getGroup() {
        return group;
    }

    public void setGroup(Integer group) {
        this.group = group;
    }
}
