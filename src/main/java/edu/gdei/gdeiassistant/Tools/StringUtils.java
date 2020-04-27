package edu.gdei.gdeiassistant.Tools;

import java.util.UUID;

public class StringUtils {

    /**
     * 将空引用字符串转变为空字符串
     *
     * @param string
     * @return
     */
    public static String nullToBlank(String string) {
        return string == null ? "" : string;
    }

    /**
     * 判断String字符串是否为Null或者空字符串
     *
     * @param string
     * @return
     */
    public static boolean isBlank(String string) {
        return string == null || string.isEmpty();
    }

    /**
     * 判断String字符串是否非Null和空字符串
     *
     * @param string
     * @return
     */
    public static boolean isNotBlank(String string) {
        return !isBlank(string);
    }

    /**
     * 获取随机字符串UUID
     *
     * @return
     */
    public static String randomUUID() {
        return UUID.randomUUID().toString();
    }
}
