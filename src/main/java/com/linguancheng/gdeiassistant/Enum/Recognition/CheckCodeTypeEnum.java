package com.linguancheng.gdeiassistant.Enum.Recognition;

public enum CheckCodeTypeEnum {

    NUMBER("n"), ENGLISH_WITH_NUMBER("en");

    CheckCodeTypeEnum(String type) {
        this.type = type;
    }

    private String type;

    public String getType() {
        return type;
    }

}
