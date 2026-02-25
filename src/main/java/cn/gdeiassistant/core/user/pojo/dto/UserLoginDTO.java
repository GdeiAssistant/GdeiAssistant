package cn.gdeiassistant.core.user.pojo.dto;

import cn.gdeiassistant.common.validgroup.User.UserLoginValidGroup;
import org.apache.ibatis.type.Alias;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.io.Serializable;

/**
 * 登录接口入参 DTO（主库 App 使用，别名固定为 UserLoginDTO 供 MyBatis 多数据源下唯一）。
 */
@Alias("UserLoginDTO")
public class UserLoginDTO implements Serializable {

    @NotBlank(message = "用户名不能为空", groups = {UserLoginValidGroup.class})
    @Size(min = 1, max = 20, message = "用户名长度超过限制", groups = {UserLoginValidGroup.class})
    private String username;

    @NotBlank(message = "密码不能为空", groups = {UserLoginValidGroup.class})
    @Size(min = 1, max = 35, message = "密码长度超过限制", groups = {UserLoginValidGroup.class})
    private String password;

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
