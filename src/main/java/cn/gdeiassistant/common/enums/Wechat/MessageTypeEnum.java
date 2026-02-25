package cn.gdeiassistant.common.enums.Wechat;

public enum MessageTypeEnum {

    TEXT("text"),
    IMAGE_TEXT("news");

    MessageTypeEnum(String type) {
        this.type = type;
    }

    private final String type;

    public String getType() {
        return type;
    }

}
