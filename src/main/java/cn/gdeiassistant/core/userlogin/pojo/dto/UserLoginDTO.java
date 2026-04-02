package cn.gdeiassistant.core.userLogin.pojo.dto;

import org.apache.ibatis.type.Alias;
import jakarta.validation.constraints.NotBlank;

import java.io.Serializable;

/**
 * 登录请求体（仅做类型包装）。与 core.user.pojo.dto.UserLoginDTO 同名，用别名避免 MyBatis 多数据源别名冲突。
 */
@Alias("UserLoginRequestDTO")
public class UserLoginDTO implements Serializable {

    @NotBlank(message = "用户名不能为空")
    private String username;

    @NotBlank(message = "密码不能为空")
    private String password;

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
