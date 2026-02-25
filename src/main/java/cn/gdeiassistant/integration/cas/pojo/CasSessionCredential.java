package cn.gdeiassistant.integration.cas.pojo;

import org.apache.http.client.CookieStore;

/**
 * CAS 会话凭证：封装 CAS 登录后的 CookieStore 与跳转到业务系统的 serviceUrl。
 */
public class CasSessionCredential {

    private CookieStore cookieStore;

    /**
     * CAS 登录成功后返回的跳转链接（通常为 web.gdei.edu.cn 的入口地址）。
     */
    private String serviceUrl;

    public CookieStore getCookieStore() {
        return cookieStore;
    }

    public void setCookieStore(CookieStore cookieStore) {
        this.cookieStore = cookieStore;
    }

    public String getServiceUrl() {
        return serviceUrl;
    }

    public void setServiceUrl(String serviceUrl) {
        this.serviceUrl = serviceUrl;
    }
}

