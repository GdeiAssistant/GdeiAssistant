package cn.gdeiassistant.core.phone.pojo.dto;

import jakarta.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 绑定手机号入参（区号 + 手机号）。
 */
public class PhoneBindDTO implements Serializable {

    @NotNull
    private Integer code;

    @NotNull
    private String phone;

    public Integer getCode() { return code; }
    public void setCode(Integer code) { this.code = code; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
}
