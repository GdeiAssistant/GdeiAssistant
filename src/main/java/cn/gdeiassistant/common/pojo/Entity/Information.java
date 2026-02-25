package cn.gdeiassistant.common.pojo.Entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.List;

@Component
@Scope("prototype")
@JsonIgnoreProperties(ignoreUnknown = true)
public class Information implements Serializable, Entity {

    /**
     * 通知公告（最新一条）
     */
    private Announcement notice;

    /**
     * 校园公众号列表
     */
    private List<WechatAccount> accounts;

    /**
     * 专题阅读列表
     */
    private List<Reading> topics;

    /**
     * 世界上的今日
     */
    private Festival festival;

    public Announcement getNotice() {
        return notice;
    }

    public void setNotice(Announcement notice) {
        this.notice = notice;
    }

    public List<WechatAccount> getAccounts() {
        return accounts;
    }

    public void setAccounts(List<WechatAccount> accounts) {
        this.accounts = accounts;
    }

    public List<Reading> getTopics() {
        return topics;
    }

    public void setTopics(List<Reading> topics) {
        this.topics = topics;
    }

    public Festival getFestival() {
        return festival;
    }

    public void setFestival(Festival festival) {
        this.festival = festival;
    }
}

