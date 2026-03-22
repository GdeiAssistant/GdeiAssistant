package cn.gdeiassistant.common.tools.Utils;

/**
 * Replaces internal deleted-account identifiers with a user-friendly label.
 */
public class AnonymizeUtils {

    private static final String DELETED_PREFIX = "del_";
    private static final String ANONYMOUS_LABEL = "已注销用户";

    public static String sanitizeUsername(String username) {
        if (username != null && username.startsWith(DELETED_PREFIX)) {
            return ANONYMOUS_LABEL;
        }
        return username;
    }
}
