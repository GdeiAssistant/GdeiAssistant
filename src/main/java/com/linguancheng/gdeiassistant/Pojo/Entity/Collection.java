package com.linguancheng.gdeiassistant.Pojo.Entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.Serializable;

@Component
@Scope("prototype")
@JsonIgnoreProperties(ignoreUnknown = true)
public class Collection implements Serializable {

    //书名
    private String bookname;

    //书目信息
    private String bookinfo;

    //馆藏数量
    private int sumCount;

    //可借数量
    private int availableCount;

    //详细信息URL
    private String detailURL;

    public String getBookinfo() {
        return bookinfo;
    }

    public void setBookinfo(String bookinfo) {
        this.bookinfo = bookinfo;
    }

    public int getSumCount() {
        return sumCount;
    }

    public void setSumCount(int sumCount) {
        this.sumCount = sumCount;
    }

    public int getAvailableCount() {
        return availableCount;
    }

    public void setAvailableCount(int availableCount) {
        this.availableCount = availableCount;
    }

    public String getDetailURL() {
        return detailURL;
    }

    public void setDetailURL(String detailURL) {
        this.detailURL = detailURL;
    }

    public String getBookname() {
        return bookname;
    }

    public void setBookname(String bookname) {
        this.bookname = bookname;
    }
}
