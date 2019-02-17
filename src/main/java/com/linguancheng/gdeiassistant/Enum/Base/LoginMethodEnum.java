package com.linguancheng.gdeiassistant.Enum.Base;

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
