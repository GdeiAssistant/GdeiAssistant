package com.linguancheng.gdeiassistant.Pojo.Entity;

import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.List;

@Component
@Scope("prototype")
@JsonIgnoreProperties(ignoreUnknown = true)
public class Charge implements Serializable {

    @JSONField(ordinal = 1)
    private String alipayURL;

    @JSONField(ordinal = 2)
    private List<Cookie> cookieList;

    public List<Cookie> getCookieList() {
        return cookieList;
    }

    public void setCookieList(List<Cookie> cookieList) {
        this.cookieList = cookieList;
    }

    public String getAlipayURL() {
        return alipayURL;
    }

    public void setAlipayURL(String alipayURL) {
        this.alipayURL = alipayURL;
    }
}
