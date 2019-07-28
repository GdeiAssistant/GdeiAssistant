package edu.gdei.gdeiassistant.Constant;

public class ErrorConstantUtils {

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

    //实名认证的身份证信息校验不通过
    public static final int ID_CARD_NOT_VERIFIED = 40015;

    //用户账号密码错误
    public static final int PASSWORD_INCORRECT = 40016;

    //查询条件不合法
    public static final int ERROR_QUERY_CONDITION = 40017;

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

    //登录凭证过期
    public static final int SESSION_INVALIDATED = 40107;

    //教务系统会话时间戳校验失败
    public static final int TIMESTAMP_INVALIDATED = 40108;

    //实名认证未通过，禁止进行操作或访问资源
    public static final int NOT_AUTHENTICATION = 40109;

    //自定义课程数量超过限制
    public static final int CUSTOM_SCHEDULE_OVER_LIMIT = 40301;

    //查询的数据不存在
    public static final int DATA_NOT_EXIST = 40401;

    //查询的用户不存在
    public static final int USER_NOT_EXIST = 40402;

    //服务器内部异常
    public static final int INTERNAL_SERVER_ERROR = 50001;

    //一键评教未开放
    public static final int EVALUATE_NOT_AVAILABLE = 50002;

    //用户教务系统账号已毕业注销
    public static final int ACCOUNT_GRADUATED = 50003;

    //网络连接超时
    public static final int NETWORK_TIMEOUT = 50301;

}
