package cn.gdeiassistant.common.exception;

/** A previous account incarnation still owns the username's cross-store resources. */
public class AccountCleanupPendingException extends IllegalStateException {
    public AccountCleanupPendingException() {
        super("账号注销清理尚未完成，请稍后重新登录");
    }
}
