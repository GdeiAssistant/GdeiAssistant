package com.linguancheng.gdeiassistant.Enum.Wechat;

public enum RequestTypeEnum {

    TODAY_SCHEDULE("课表1"),

    GRADE("成绩");

    RequestTypeEnum(String type) {
        this.type = type;
    }

    private String type;

    public String getType() {
        return type;
    }

    public static RequestTypeEnum getRequestTypeEnumByFunctionName(String functionName){
        for(RequestTypeEnum requestTypeEnum:RequestTypeEnum.values()){
            if(requestTypeEnum.getType().equalsIgnoreCase(functionName)){
                return requestTypeEnum;
            }
        }
        return null;
    }
}
