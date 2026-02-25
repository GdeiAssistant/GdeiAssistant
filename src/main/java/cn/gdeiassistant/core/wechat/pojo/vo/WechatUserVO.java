package cn.gdeiassistant.core.wechat.pojo.vo;

import java.io.Serializable;

/**
 * 返回前端的微信用户标识（openid/unionid），仅做类型包装。
 */
public class WechatUserVO implements Serializable {

    private String openid;
    private String unionid;

    public String getOpenid() { return openid; }
    public void setOpenid(String openid) { this.openid = openid; }
    public String getUnionid() { return unionid; }
    public void setUnionid(String unionid) { this.unionid = unionid; }
}
