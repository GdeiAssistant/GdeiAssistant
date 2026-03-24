package cn.gdeiassistant.common.constant;

public class SettingConstantUtils {

    /** 登录拦截器放行的 URL 前缀或路径 */
    public static final String[] LOGIN_INTERCEPTOR_EXCEPTION_LIST = {
            "/login",
            "/logout",
            // "/cron" — removed: cron endpoints now require X-Cron-Secret header authentication
            "/api/auth/login",
            "/api/auth/logout",
            "/api/module",
            "/download",
            "/agreement",
            "/policy",
            "/license",
            "/announcement",
            "/about",
            "/covid19"
    };
}
