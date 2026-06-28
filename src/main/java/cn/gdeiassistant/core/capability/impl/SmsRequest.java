package cn.gdeiassistant.core.capability.impl;

public class SmsRequest {

    private final int code;
    private final String phone;
    private final boolean isGlobal;
    private final int areaCode;

    public SmsRequest(int code, String phone, boolean isGlobal) {
        this(code, phone, isGlobal, 0);
    }

    public SmsRequest(int code, String phone, boolean isGlobal, int areaCode) {
        this.code = code;
        this.phone = phone;
        this.isGlobal = isGlobal;
        this.areaCode = areaCode;
    }

    public int getCode() { return code; }
    public String getPhone() { return phone; }
    public boolean isGlobal() { return isGlobal; }
    public int getAreaCode() { return areaCode; }
}
