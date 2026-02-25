package cn.gdeiassistant.core.information.pojo.entity;

import java.io.Serializable;
import java.util.Date;

/**
 * 阅读表 reading 的持久化映射，column 与库表一致。
 */
public class ReadingEntity implements Serializable {

    private String id;
    private String title;
    private String description;
    private String link;
    private Date createTime;

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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }
}
