package com.linguancheng.gdeiassistant.Enum.Authentication;

public enum AuthenticationTypeEnum {

    AUTHENTICATION_DELETED("-1"),

    AUTHENTICATE_WITH_CAS_SYSTEM("0"),

    AUTHENTICATE_WITH_UPLOAD_IDENTITY_CARD("1");

    private String type;

    public String getType() {
        return type;
    }

    AuthenticationTypeEnum(String type) {
        this.type = type;
    }

    public static AuthenticationTypeEnum getEnumByValue(String value) {
        for (AuthenticationTypeEnum authenticationTypeEnum : values()) {
            if (authenticationTypeEnum.type.equals(value)) {
                return authenticationTypeEnum;
            }
        }
        return null;
    }
}
