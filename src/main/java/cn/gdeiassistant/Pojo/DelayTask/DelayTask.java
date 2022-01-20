package cn.gdeiassistant.Pojo.DelayTask;

import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

public class DelayTask implements Delayed {

    //延时任务元素基类，其子类类型决定延时任务内容
    private final DelayTaskElement delayTaskElement;

    //单位为毫秒，过期时间为用户输入的延时时间加上现在时间
    private final long expire;

    public DelayTask(DelayTaskElement delayTaskElement, long expire, TimeUnit unit) {
        super();
        this.delayTaskElement = delayTaskElement;
        this.expire = unit.toNanos(expire) + System.currentTimeMillis();
    }

    public DelayTaskElement getDelayTaskElement() {
        return delayTaskElement;
    }

    public long getExpire() {
        return expire;
    }

    @Override
    public long getDelay(TimeUnit unit) {
        return unit.convert(this.expire - System.currentTimeMillis(), unit);
    }

    @Override
    public int compareTo(Delayed o) {
        long delta = getDelay(TimeUnit.NANOSECONDS) - o.getDelay(TimeUnit.NANOSECONDS);
        return (int) delta;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DelayTask delayTask = (DelayTask) o;

        return delayTaskElement.getId().equals(delayTask.delayTaskElement.getId());
    }

    @Override
    public int hashCode() {
        return delayTaskElement.hashCode();
    }
}
