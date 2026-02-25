package cn.gdeiassistant.core.userLogin.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

@Service
public class AutoLoginService {

    private final int AUTOLOGIN_NOT = 0;
    private final int AUTOLOGIN_SESSION = 1;
    private final int AUTOLOGIN_COOKIE = 2;

    @Autowired
    private UserCertificateService userCertificateService;

    /**
     * 检查自动登录状态
     *
     * @param request
     * @return 自动登录状态
     */
    public int CheckAutoLogin(HttpServletRequest request) {
        String sessionId = (String) request.getAttribute("sessionId");
        if (sessionId != null && userCertificateService.getUserLoginCertificate(sessionId) != null) {
            return AUTOLOGIN_SESSION;
        } else {
            Cookie[] cookie = request.getCookies();
            if (cookie != null && cookie.length > 0) {
                String cookieId = "";
                for (int i = 0; i < cookie.length; i++) {
                    if (cookie[i].getName().equals("cookieId")) {
                        cookieId = cookie[i].getValue();
                    }
                }
                if (!cookieId.trim().isEmpty()) {
                    return AUTOLOGIN_COOKIE;
                }
            }
        }
        return AUTOLOGIN_NOT;
    }

}
