package cn.gdeiassistant.Pojo.Entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * 用户使用移动端登录后，服务端会在系统中生成一个令牌信息，包含用户名、时间戳等信息
 * 系统生成权限令牌的同时，还将对令牌进行签名，将签名信息返回给移动端
 * 权限令牌有效期为7天
 */

@Component
@Scope("prototype")
@JsonIgnoreProperties(ignoreUnknown = true)
public class AccessToken extends Token {

    /**
     * 用户名
     */
    private String username;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
