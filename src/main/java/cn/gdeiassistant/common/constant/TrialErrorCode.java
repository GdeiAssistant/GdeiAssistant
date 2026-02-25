package cn.gdeiassistant.common.constant;

/**
 * 样板间与头像等相关的统一错误枚举。
 */
public enum TrialErrorCode {

    SUCCESS(200, "成功"),

    NOT_SUPPORTED(400301, "测试账号不支持该功能"),

    INVALID_TOKEN(400302, "登录已失效，请重新登录"),

    AVATAR_NOT_CONFIGURED(400303, "头像存储未配置，请检查 R2 配置"),

    CLOSE_ACCOUNT_DISABLED(400304, "测试账号不支持注销操作"),

    DATA_NOT_FOUND(400305, "样板间数据未初始化，请检查数据库配置");

    private final int code;
    private final String message;

    TrialErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}

