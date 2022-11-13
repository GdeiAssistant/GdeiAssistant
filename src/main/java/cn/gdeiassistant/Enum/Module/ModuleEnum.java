package cn.gdeiassistant.Enum.Module;

public enum ModuleEnum {

    EMAIL("邮件", "/email/email.properties"),
    ENCRYPTION("安全加密", "/encrypt/encryptor.properties"),
    ALIPAY_MINIPROGRAM("支付宝小程序", "/alipay/app.properties"),
    QQ_MINIPROGRAM("QQ小程序", "/qq/app.properties"),
    ALIYUN_API("阿里云API服务", "/api/aliyun.properties"),
    ALIYUN_SMS("阿里云短信服务", "/aliyun/sms.properties"),
    ALIYUN_OSS("阿里云对象存储服务", "/aliyun/oss.properties及/aliyun/oss_token.properties"),
    JISU("极速数据API服务", "/api/jisu.properties"),
    JWT("JSON Web Token", "/jwt/secret.properties"),
    NEWS("新闻通知", "/news/sms.properties"),
    REPLAY_ATTACKS_VALIDATE("防重放攻击验证", "/validate/token.properties"),
    WECHAT_OFFICIAL_ACCOUNT("微信公众号", "/wechat/sms.properties"),
    WECHAT_MINI_PROGRAM("微信小程序", "/wechat/app.properties");

    ModuleEnum(String name, String location) {
        this.name = name;
        this.location = location;
    }

    private final String name;

    public String getName() {
        return name;
    }

    private final String location;

    public String getLocation() {
        return location;
    }
}
