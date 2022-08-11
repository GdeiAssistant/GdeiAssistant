package cn.gdeiassistant.Enum.Recognition;

public enum CheckCodeTypeEnum {

    NUMBER("n"), ENGLISH_WITH_NUMBER("en"), ENGLISH("e"), CHINESE("c");

    CheckCodeTypeEnum(String type) {
        this.type = type;
    }

    private final String type;

    public String getType() {
        return type;
    }

}
