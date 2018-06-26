package com.linguancheng.gdeiassistant.Pojo.Entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

@Component
@Scope("prototype")
@JsonIgnoreProperties(ignoreUnknown = true)
public class NewInfo implements Serializable {

    private int id;

    private int type;

    private String title;

    private Date publishDate;

    private String content;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPublishDate() {
        return new SimpleDateFormat("yyyy-MM-dd").format(publishDate);
    }

    public void setPublishDate(String publishTime) throws ParseException {
        this.publishDate = new SimpleDateFormat("yyyy-MM-dd").parse(publishTime);
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}
