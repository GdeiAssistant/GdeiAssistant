package cn.gdeiassistant.common.pojo.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Information implements Serializable, Entity {

    /**
     * 通知公告（最新一条）
     */
    private Announcement notice;

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

    public Festival getFestival() {
        return festival;
    }

    public void setFestival(Festival festival) {
        this.festival = festival;
    }
}

