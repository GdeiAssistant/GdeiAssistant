package cn.gdeiassistant.common.account;

/**
 * Identity snapshot shared with each domain while the account is being closed.
 *
 * @param username       login name being closed
 * @param anonymizedName login name that replaces the original in community content
 * @param userId         primary key of the ordered app_user row, null for legacy records
 */
public record AccountClosure(String username, String anonymizedName, Long userId) {
}
