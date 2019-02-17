package edu.gdei.gdeiassistant.Pojo.CetQuery;

import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.Size;

public class CetQuery {

    @NotBlank(message = "姓名不能为空")
    @Size(min = 1, max = 20, message = "姓名长度超过限制")
    private String name;

    @NotBlank(message = "准考证号不能为空")
    @Size(min = 15, max = 15, message = "准考证号格式不正确")
    private String number;

    @NotBlank(message = "验证码不能为空")
    private String checkcode;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getCheckcode() {
        return checkcode;
    }

    public void setCheckcode(String checkcode) {
        this.checkcode = checkcode;
    }
}
