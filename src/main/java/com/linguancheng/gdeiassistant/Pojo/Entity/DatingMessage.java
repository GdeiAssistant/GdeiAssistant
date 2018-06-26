package com.linguancheng.gdeiassistant.Pojo.Entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.Serializable;

/**
 * 广东二师助手团队 林冠成 版权所有
 * All rights reserved © 2016 - 2018
 * Author:林冠成
 * Date:2018/5/7
 */
@Component
@Scope("prototype")
@JsonIgnoreProperties(ignoreUnknown = true)
public class DatingMessage implements Serializable {

    /**
     * 卖室友消息的主键ID
     */
    private Integer messageId;

    /**
     * 接收该消息的用户名
     */
    private String username;

    private DatingPick datingPick;

    /**
     * 消息类型，0表示收到了撩一下的通知，1表示被撩者已接受/拒绝撩一下请求
     */
    private Integer type;

    /**
     * 消息是否已读，0表示消息未读，1表示消息已读
     */
    private Integer state;

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public Integer getState() {
        return state;
    }

    public void setState(Integer state) {
        this.state = state;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public DatingPick getDatingPick() {
        return datingPick;
    }

    public void setDatingPick(DatingPick datingPick) {
        this.datingPick = datingPick;
    }

    public Integer getMessageId() {
        return messageId;
    }

    public void setMessageId(Integer messageId) {
        this.messageId = messageId;
    }
}
