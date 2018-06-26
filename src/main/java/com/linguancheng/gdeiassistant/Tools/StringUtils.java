package com.linguancheng.gdeiassistant.Tools;

import java.util.UUID;

public class StringUtils {

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
