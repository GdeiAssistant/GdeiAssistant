package cn.gdeiassistant.core.phone.pojo.vo;

import java.io.Serializable;

/**
 * 手机号视图（可屏蔽敏感信息，如不返回验证码）。
 */
public class PhoneVO implements Serializable {

    private String username;
    private String phone;

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
}
