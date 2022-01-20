package cn.gdeiassistant.Pojo.DelayTask;

import java.util.UUID;

public class DelayTaskElement {

    //延时任务元素编号，用于标记和终止延时任务
    private String id;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public DelayTaskElement() {
        this.id = UUID.randomUUID().toString().replace("-", "");
    }

    public DelayTaskElement(String id) {
        this.id = id;
    }
}
