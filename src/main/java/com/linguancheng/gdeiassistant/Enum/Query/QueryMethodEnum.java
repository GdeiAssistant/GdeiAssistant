package com.gdeiassistant.gdeiassistant.Enum.Query;

public enum QueryMethodEnum {

    CACHE_FIRST("0"),
    QUERY_ONLY("1"),
    CACHE_ONLY("2");

    QueryMethodEnum(String method) {
        this.method = method;
    }

    private String method;

    public String getMethodValue() {
        return method;
    }

    public static QueryMethodEnum getEnumByValue(String value) {
        for (QueryMethodEnum queryMethodEnum : values()) {
            if (queryMethodEnum.method.equals(value)) {
                return queryMethodEnum;
            }
        }
        return null;
    }

}
