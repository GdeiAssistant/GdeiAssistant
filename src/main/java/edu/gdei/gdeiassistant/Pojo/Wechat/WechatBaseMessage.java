package edu.gdei.gdeiassistant.Pojo.Wechat;

public class WechatBaseMessage {

    //开发者微信号
    private String ToUserName;

    //发送方帐号
    private String FromUserName;

    //消息创建时间
    private long CreateTime;

    //消息类型
    private String MsgType;

    public String getToUserName() {
        return ToUserName;
    }

    public void setToUserName(String toUserName) {
        this.ToUserName = toUserName;
    }

    public String getFromUserName() {
        return FromUserName;
    }

    public void setFromUserName(String fromUserName) {
        this.FromUserName = fromUserName;
    }

    public long getCreateTime() {
        return CreateTime;
    }

    public void setCreateTime(long createTime) {
        this.CreateTime = createTime;
    }

    public String getMsgType() {
        return MsgType;
    }

    protected void setMsgType(String msgType) {
        this.MsgType = msgType;
    }
}
