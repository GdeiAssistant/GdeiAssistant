package edu.gdei.gdeiassistant.Enum.Wechat;

public enum MessageTypeEnum {

    TEXT("text"),
    IMAGE_TEXT("news");

    MessageTypeEnum(String type) {
        this.type = type;
    }

    private String type;

    public String getType() {
        return type;
    }

}
