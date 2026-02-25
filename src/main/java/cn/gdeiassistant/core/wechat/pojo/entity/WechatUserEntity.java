package cn.gdeiassistant.core.wechat.pojo.entity;

import java.io.Serializable;

/**
 * wechat_user 表持久化映射（wechat_id, username），仅做类型包装。
 */
public class WechatUserEntity implements Serializable {

    private String wechatId;
    private String username;

    public String getWechatId() { return wechatId; }
    public void setWechatId(String wechatId) { this.wechatId = wechatId; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
}
