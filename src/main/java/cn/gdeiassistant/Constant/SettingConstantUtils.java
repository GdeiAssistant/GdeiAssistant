package cn.gdeiassistant.Constant;

import java.util.Map;

import static java.util.Map.entry;

public class SettingConstantUtils {

    public static final String[] LOGIN_INTERCEPTOR_EXCEPTION_LIST = {
            "/login", //登录账号
            "/logout", //退出账号
            "/cron", //Cron作业
            "/api/userlogin", //用户登录接口
            "/download", //下载接口
            "/agreement", //用户协议
            "/policy", //政策准则
            "/license", //开源许可
            "/announcement", //公告声明
            "/about", //软件说明
            "/rest", //Restful API接口
            "/wechat", //微信API接口
            "/qq", //QQ API接口
            "/alipay" //支付宝API接口
    };

    public static final String[] AUTHENTICATION_INTERCEPTOR_EXCEPTION_LIST = {
            "/login", //登录账号
            "/logout", //退出账号
            "/cron", //Cron作业
            "/api/userlogin", //用户登录接口
            "/rest/userlogin", //用户Restful API登录接口
            "/rest/token", //权限令牌操作
            "/authentication", //实名认证页面
            "/api/authentication", //实名认证接口
            "/close", //注销页面
            "/api/close", //注销接口
            "/download", //下载接口
            "/agreement", //用户协议
            "/policy", //政策准则
            "/license", //开源许可
            "/announcement", //公告声明
            "/about", //软件说明
            "/wechat", //微信API接口
            "/qq", //QQ API接口
            "/alipay", //支付宝API接口
    };

    public static final Map<String, String> VIEW_CONTROLLER_NAME_MAP = Map.ofEntries(
            entry("/", "/About/index"),
            entry("/about", "About/index"),
            entry("/about/security", "About/security"),
            entry("/about/account", "About/account"),
            entry("/license", "About/license"),
            entry("/agreement", "AgreementAndPolicy/agreement"),
            entry("/policy/cookie", "AgreementAndPolicy/cookiePolicy"),
            entry("/policy/privacy", "AgreementAndPolicy/privacyPolicy"),
            entry("/policy/social", "AgreementAndPolicy/socialPolicy"),
            entry("/about/graduation", "AgreementAndPolicy/graduationPolicy"),
            entry("/announcement/equalrights", "Announcement/equalrights"),
            entry("/announcement/suicideprevention", "Announcement/suicideprevention")
    );
}
