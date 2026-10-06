package cn.gdeiassistant.core.social.exception;

/**
 * 社交契约稳定错误。HTTP 状态与业务 code 一致，errorCode 供客户端判断。
 */
public class SocialException extends RuntimeException {

    private final int httpStatus;
    private final String errorCode;

    public SocialException(int httpStatus, String errorCode, String message) {
        super(message);
        this.httpStatus = httpStatus;
        this.errorCode = errorCode;
    }

    public int getHttpStatus() {
        return httpStatus;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public static SocialException authRequired() {
        return new SocialException(401, "AUTH_REQUIRED", "未登录或会话已失效");
    }

    public static SocialException userNotFound() {
        return new SocialException(404, "USER_NOT_FOUND", "用户不存在");
    }

    public static SocialException conversationNotFound() {
        return new SocialException(404, "CONVERSATION_NOT_FOUND", "会话不存在");
    }

    public static SocialException contactUnavailable() {
        return new SocialException(403, "CONTACT_UNAVAILABLE", "无法联系该用户");
    }

    public static SocialException privacyRestricted() {
        return new SocialException(403, "PRIVACY_RESTRICTED", "对方隐私设置不允许私信");
    }

    public static SocialException invalidRequest(String message) {
        return new SocialException(400, "INVALID_REQUEST", message);
    }

    public static SocialException clientMessageConflict() {
        return new SocialException(409, "CLIENT_MESSAGE_CONFLICT", "客户端消息冲突");
    }

    public static SocialException rateLimited() {
        return new SocialException(429, "RATE_LIMITED", "请求过于频繁");
    }

    public static SocialException imageUnavailable() {
        return new SocialException(503, "SOCIAL_IMAGE_UNAVAILABLE", "图片私信暂不可用");
    }
}
