package cn.gdeiassistant.Enum.Method;

public enum QueryMethodEnum {

    CACHE_FIRST(0),
    QUERY_ONLY(1),
    CACHE_ONLY(2);

    private Integer method;

    QueryMethodEnum(Integer method) {
        this.method = method;
    }

    public static QueryMethodEnum getEnumByValue(String value) {
        for (QueryMethodEnum queryMethodEnum : values()) {
            if (queryMethodEnum.method.equals(Integer.valueOf(value))) {
                return queryMethodEnum;
            }
        }
        return null;
    }

    public Integer getMethod() {
        return method;
    }

}
