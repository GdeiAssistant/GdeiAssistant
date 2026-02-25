package cn.gdeiassistant.core.wechat.pojo.vo;

import java.io.Serializable;

/**
 * 微信小程序/扫码登录结果视图（openid、session_key、unionid），仅做类型包装。
 */
public class WechatAppLoginVO implements Serializable {

    private String openid;
    private String session_key;
    private String unionid;

    public String getOpenid() { return openid; }
    public void setOpenid(String openid) { this.openid = openid; }
    public String getSession_key() { return session_key; }
    public void setSession_key(String session_key) { this.session_key = session_key; }
    public String getUnionid() { return unionid; }
    public void setUnionid(String unionid) { this.unionid = unionid; }
}
