package com.linguancheng.gdeiassistant.Pojo.CollectionQuery;

import org.hibernate.validator.constraints.NotBlank;

public class CollectionDetailQuery {

    @NotBlank(message = "图书详细信息URL不能为空")
    private String url;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
