package cn.gdeiassistant.Enum.UserData;

public enum ExportStateEnum {

    NOT_EXPORT(0), EXPORTING(1), EXPORTED(2);

    private Integer value;

    public Integer getValue() {
        return value;
    }

    ExportStateEnum(Integer value) {
        this.value = value;
    }
}
