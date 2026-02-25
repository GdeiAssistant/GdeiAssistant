package cn.gdeiassistant.core.information.pojo.entity;

import java.io.Serializable;
import java.util.Date;

/**
 * 公告表 announcement 的持久化映射，column 与库表一致。
 */
public class AnnouncementEntity implements Serializable {

    private String id;
    private String title;
    private String content;
    private Date publishTime;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Date getPublishTime() {
        return publishTime;
    }

    public void setPublishTime(Date publishTime) {
        this.publishTime = publishTime;
    }
}
