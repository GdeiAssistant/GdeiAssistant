package cn.gdeiassistant.core.wechat.pojo.dto;

import java.io.Serializable;

/**
 * 微信服务器 XML 消息入参 DTO（与 WechatBaseMessage 字段一致），不改变 XML 序列化逻辑。
 */
public class WechatMessageDTO implements Serializable {

    private String ToUserName;
    private String FromUserName;
    private long CreateTime;
    private String MsgType;

    public String getToUserName() { return ToUserName; }
    public void setToUserName(String toUserName) { ToUserName = toUserName; }
    public String getFromUserName() { return FromUserName; }
    public void setFromUserName(String fromUserName) { FromUserName = fromUserName; }
    public long getCreateTime() { return CreateTime; }
    public void setCreateTime(long createTime) { CreateTime = createTime; }
    public String getMsgType() { return MsgType; }
    public void setMsgType(String msgType) { MsgType = msgType; }
}
