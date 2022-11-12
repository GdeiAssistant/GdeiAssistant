package cn.gdeiassistant.Enum.IPAddress;

public enum IPAddressEnum {

    LOGIN(0), POST(1);

    private final Integer value;

    public Integer getValue() {
        return value;
    }

    IPAddressEnum(Integer value) {
        this.value = value;
    }
}
