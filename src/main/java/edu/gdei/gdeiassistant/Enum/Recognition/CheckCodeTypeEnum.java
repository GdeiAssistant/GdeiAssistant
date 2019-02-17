package edu.gdei.gdeiassistant.Enum.Recognition;

public enum CheckCodeTypeEnum {

    NUMBER("n"), ENGLISH_WITH_NUMBER("en"), ENGLISH("e"), CHINESE("c");

    CheckCodeTypeEnum(String type) {
        this.type = type;
    }

    private String type;

    public String getType() {
        return type;
    }

}
