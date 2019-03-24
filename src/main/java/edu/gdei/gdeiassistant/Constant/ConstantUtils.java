package edu.gdei.gdeiassistant.Constant;

public class ConstantUtils {

    //请求参数不合法
    public static final int INCORRECT_REQUEST_PARAM = 40001;

    //请求数据摘要不匹配
    public static final int REQUEST_SIGN_INVALID = 40002;

    //请求时间戳校验失败
    public static final int REQUEST_TIMESTAMP_INVALID = 40003;

    //重复提交的请求
    public static final int REPLAY_REQUEST = 40004;

    //充值金额不合法
    public static final int CHARGE_AMOUNT_NOT_AVAILABLE = 40005;

    //实名认证身份证照片模糊
    public static final int ID_CARD_BLURRED = 40006;

    //实名认证身份证照片为复印件
    public static final int ID_CARD_COPY_TYPE = 40007;

    //实名认证身份证照片被编辑过
    public static final int ID_CARD_EDITED = 40008;

    //实名认证身份证照片反光或过曝
    public static final int ID_CARD_OVER_EXPOSURE = 40009;

    //实名认证身份证照片正反面颠倒
    public static final int ID_CARD_REVERSED_SIDE = 40010;

    //实名认证身份证照片为翻拍
    public static final int ID_CARD_SCREEN_TYPE = 40011;

    //实名认证身份证照片为临时身份证
    public static final int ID_CARD_TEMPORARY_TYPE = 40012;

    //实名认证上传的图片没有身份证信息
    public static final int NON_ID_CARD_INFO = 40013;

    //实名认证上传的非第二代居民身份证
    public static final int OTHER_TYPE_CARD = 40014;

    //用户账号密码错误
    public static final int PASSWORD_INCORRECT = 40015;

    //权限令牌过期
    public static final int TOKEN_EXPIRED_EXCEPTION = 40101;

    //异常登录地
    public static final int UNUSUAL_LOCATION_EXCEPTION = 40102;

    //令牌校验失败
    public static final int TOKEN_NOT_MATCHING = 40103;

    //令牌校验服务异常
    public static final int TOKEN_SERVER_ERROR = 40104;

    //充值安全校验不通过
    public static final int CHARGE_SECURITY_INVALID = 40105;

    //用户所属用户组没有权限访问该资源
    public static final int USER_GROUP_NO_ACCESS = 40106;

    //自定义课程数量超过限制
    public static final int CUSTOM_SCHEDULE_OVER_LIMIT = 40301;

    //查询的数据不存在
    public static final int DATA_NOT_EXIST = 40401;

    //服务器内部异常
    public static final int INTERNAL_SERVER_ERROR = 50001;

    //网络连接超时
    public static final int NETWORK_TIMEOUT = 50301;

}
