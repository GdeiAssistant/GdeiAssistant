package cn.gdeiassistant.Pojo.UserLogin;

import cn.gdeiassistant.Pojo.Entity.User;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
public class UserCertificate {

    //校园网络用户类
    private User user;

    //校园网络账号加密值
    private String keycode;

    //校园网络账号学号
    private String number;

    //校园网络登录时间戳
    private Long timestamp;

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
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

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }
}
