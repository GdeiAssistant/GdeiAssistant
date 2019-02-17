package edu.gdei.gdeiassistant.Pojo.Wechat;

import edu.gdei.gdeiassistant.Enum.Wechat.MessageTypeEnum;

public class WechatTextMessage extends WechatBaseMessage {

    //消息内容
    private String Content;

    public String getContent() {
        return Content;
    }

    public void setContent(String content) {
        this.Content = content;
    }

    public WechatTextMessage() {
        super.setMsgType(MessageTypeEnum.TEXT.getType());
    }

    public WechatTextMessage(WechatBaseMessage wechatBaseMessage) {
        super.setFromUserName(wechatBaseMessage.getFromUserName());
        super.setToUserName(wechatBaseMessage.getToUserName());
        super.setCreateTime(wechatBaseMessage.getCreateTime());
        super.setMsgType(MessageTypeEnum.TEXT.getType());
    }

    public WechatTextMessage(WechatBaseMessage wechatBaseMessage, String content) {
        super.setFromUserName(wechatBaseMessage.getFromUserName());
        super.setToUserName(wechatBaseMessage.getToUserName());
        super.setCreateTime(wechatBaseMessage.getCreateTime());
        super.setMsgType(MessageTypeEnum.TEXT.getType());
        this.setContent(content);
    }
}
