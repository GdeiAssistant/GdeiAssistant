package cn.gdeiassistant.core.delayTask.pojo;

public class SessionAttributeExpireDelayTaskElement extends DelayTaskElement {

    private String key;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public SessionAttributeExpireDelayTaskElement() {

    }

    public SessionAttributeExpireDelayTaskElement(String key) {
        this.key = key;
    }
}
