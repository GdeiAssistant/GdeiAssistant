package com.linguancheng.gdeiassistant.Cookie;

import org.apache.http.client.CookieStore;
import org.apache.http.cookie.Cookie;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.web.HttpSessionRequiredException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.List;

@Component
@Scope("singleton")
public class HttpClientCookieManager {

    /**
     * 清空缓存Cookie
     *
     * @param request
     */
    public void clearCookie(HttpServletRequest request) {
        HttpSession httpSession = request.getSession();
        if (httpSession != null) {
            if (httpSession.getAttribute("cookieStore") != null) {
                CookieStore cookieStore = (CookieStore) httpSession.getAttribute("cookieStore");
                cookieStore.clear();
                httpSession.removeAttribute("cookieStore");
            }
        }
    }

    /**
     * 获取缓存Cookie
     *
     * @param request
     * @return
     */
    public List<Cookie> getCookieList(HttpServletRequest request) throws HttpSessionRequiredException {
        HttpSession httpSession = request.getSession();
        if (httpSession != null) {
            if (httpSession.getAttribute("cookieStore") != null) {
                CookieStore cookieStore = (CookieStore) httpSession.getAttribute("cookieStore");
                return cookieStore.getCookies();
            }
        }
        throw new HttpSessionRequiredException("Session不存在，用户可能禁用了Cookie");
    }
}
