package cn.gdeiassistant.Constant;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SettingConstantUtils {

    public static final String[] LOGIN_INTERCEPTOR_EXCEPTION_LIST = {
            "/login", //登录账号
            "/logout", //退出账号
            "/cron", //Cron作业
            "/api/userlogin", //用户登录接口
            "/api/module", //功能接口
            "/download", //下载接口
            "/agreement", //用户协议
            "/policy", //政策准则
            "/license", //开源许可
            "/announcement", //公告声明
            "/about", //软件说明
            "/rest", //Restful API接口
            "/wechat", //微信API接口
            "/qq", //QQ API接口
            "/alipay", //支付宝API接口
            "/covid19" //疫情防控专题
    };

    public static final Map<String, String> VIEW_CONTROLLER_NAME_MAP =
            Stream.of(new String[][]{
                    {"/", "About/index"},
                    {"/covid19", "Covid19/index"},
                    {"/index", "Index/index"},
                    {"/about", "About/index"},
                    {"/about/download", "About/download"},
                    {"/about/security", "About/security"},
                    {"/about/account", "About/account"},
                    {"/license", "About/license"},
                    {"/agreement", "AgreementAndPolicy/agreement"},
                    {"/policy/cookie", "AgreementAndPolicy/cookiePolicy"},
                    {"/policy/privacy", "AgreementAndPolicy/privacyPolicy"},
                    {"/policy/social", "AgreementAndPolicy/socialPolicy"},
                    {"/policy/deletion", "AgreementAndPolicy/deletionPolicy"},
                    {"/policy/intellectualproperty", "AgreementAndPolicy/intellectualProperty"},
                    {"/announcement/suicideprevention", "Announcement/suicidePrevention"}
            }).collect(Collectors.toMap(data -> data[0], data -> data[1]));
}
