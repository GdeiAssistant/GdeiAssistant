package cn.gdeiassistant.common.pojo.entity;

import java.io.Serializable;
import java.util.Date;

/**
 * Persistence entity mapped to the {@code announcement} table.
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
