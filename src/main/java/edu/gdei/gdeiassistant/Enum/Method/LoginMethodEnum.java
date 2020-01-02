package edu.gdei.gdeiassistant.Enum.Method;

public enum LoginMethodEnum {

    QUICK_LOGIN(0),
    CAS_LOGIN(1);

    private Integer method;

    LoginMethodEnum(Integer method) {
        this.method = method;
    }

    public static LoginMethodEnum getEnumByValue(String value) {
        for (LoginMethodEnum loginMethodEnum : values()) {
            if (loginMethodEnum.method.equals(Integer.valueOf(value))) {
                return loginMethodEnum;
            }
        }
        return null;
    }

    public Integer getMethod() {
        return method;
    }
}
