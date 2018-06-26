package com.linguancheng.gdeiassistant.Pojo.Entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.apache.http.cookie.Cookie;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.List;

@Component
@Scope("prototype")
@JsonIgnoreProperties(ignoreUnknown = true)
public class Charge implements Serializable {

    private String alipayURL;

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
