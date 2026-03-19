package cn.gdeiassistant.common.pojo.Entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.Serializable;

@Component
@Scope("prototype")
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

