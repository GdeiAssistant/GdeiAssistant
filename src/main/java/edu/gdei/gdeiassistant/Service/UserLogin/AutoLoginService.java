package edu.gdei.gdeiassistant.Service.UserLogin;

import org.springframework.stereotype.Service;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

@Service
public class AutoLoginService {

    private final int AUTOLOGIN_NOT = 0;
    private final int AUTOLOGIN_SESSION = 1;
    private final int AUTOLOGIN_COOKIE = 2;

    /**
     * 检查自动登录状态
     *
     * @param request
     * @return 自动登录状态
     */
    public int CheckAutoLogin(HttpServletRequest request) {
        HttpSession httpSession = request.getSession();
        if (httpSession != null && httpSession.getAttribute("username") != null && httpSession.getAttribute("password") != null) {
            return AUTOLOGIN_SESSION;
        } else {
            Cookie cookie[] = request.getCookies();
            if (cookie != null && cookie.length > 0) {
                String username = "";
                String password = "";
                for (int i = 0; i < cookie.length; i++) {
                    if (cookie[i].getName().equals("username")) {
                        username = cookie[i].getValue();
                    }
                    if (cookie[i].getName().equals("password")) {
                        password = cookie[i].getValue();
                    }
                }
                if (!username.trim().isEmpty() && !password.trim().isEmpty()) {
                    return AUTOLOGIN_COOKIE;
                }
            }
        }
        return AUTOLOGIN_NOT;
    }

}
