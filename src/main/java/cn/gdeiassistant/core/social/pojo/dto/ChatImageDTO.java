package cn.gdeiassistant.core.social.pojo.dto;

import java.io.Serializable;

public class ChatImageDTO implements Serializable {
    private String url;
    private int width;
    private int height;
    private int size;
    private String contentType;

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
    public int getWidth() { return width; }
    public void setWidth(int width) { this.width = width; }
    public int getHeight() { return height; }
    public void setHeight(int height) { this.height = height; }
    public int getSize() { return size; }
    public void setSize(int size) { this.size = size; }
    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }
}
