package cn.gdeiassistant.common.account;

/**
 * One domain reason that blocks closure; {@code advice} tells the user how to clear it.
 */
public record AccountCloseBlocker(String reason, String advice) {
}
