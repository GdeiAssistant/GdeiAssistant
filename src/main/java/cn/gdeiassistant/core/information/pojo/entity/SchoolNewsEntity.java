package cn.gdeiassistant.core.information.pojo.entity;

import java.io.Serializable;
import java.util.Date;

/**
 * 校园新闻 MongoDB 集合 new 的持久化映射（与 NewInfo 字段一致）。
 */
public class SchoolNewsEntity implements Serializable {

    private String id;
    private Integer type;
    private String title;
    private Date publishDate;
    private String content;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Date getPublishDate() {
        return publishDate;
    }

    public void setPublishDate(Date publishDate) {
        this.publishDate = publishDate;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
