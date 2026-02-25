package cn.gdeiassistant.core.userLogin.pojo.entity;

import cn.gdeiassistant.common.pojo.Entity.User;

/**
 * 用户会话凭证实体（校园网 User + keycode/number/timestamp），仅做类型包装，不改变安全逻辑。
 */
public class UserCertificateEntity {

    private User user;
    private String keycode;
    private String number;
    private Long timestamp;

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public String getKeycode() { return keycode; }
    public void setKeycode(String keycode) { this.keycode = keycode; }
    public String getNumber() { return number; }
    public void setNumber(String number) { this.number = number; }
    public Long getTimestamp() { return timestamp; }
    public void setTimestamp(Long timestamp) { this.timestamp = timestamp; }
}
