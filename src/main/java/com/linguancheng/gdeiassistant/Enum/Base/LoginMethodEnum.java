package com.linguancheng.gdeiassistant.Enum.Base;

/**
 * 广东二师助手团队 林冠成 版权所有
 * All rights reserved © 2016 - 2018
 * Author:林冠成
 * Date:2018/11/12
 */
public enum LoginMethodEnum {

    QUICK_LOGIN("0"),
    CAS_LOGIN("1");

    LoginMethodEnum(String method) {
        this.method = method;
    }

    private String method;

    public String getMethod() {
        return method;
    }

    public static LoginMethodEnum getEnumByValue(String value) {
        for (LoginMethodEnum loginMethodEnum : values()) {
            if (loginMethodEnum.method.equals(value)) {
                return loginMethodEnum;
            }
        }
        return null;
    }
}
