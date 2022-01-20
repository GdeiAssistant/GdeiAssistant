package cn.gdeiassistant.Tools.SpringUtils;

import cn.gdeiassistant.Pojo.DelayTask.DelayTask;
import cn.gdeiassistant.Pojo.DelayTask.DelayTaskElement;
import cn.gdeiassistant.Pojo.DelayTask.SessionAttributeExpireDelayTaskElement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import javax.servlet.ServletContext;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.TimeUnit;

@Component
public class DelayTaskUtils {

    private final DelayQueue<DelayTask> delayQueue = new DelayQueue<>();

    @Autowired
    private ServletContext servletContext;

    @EventListener(DelayTaskUtils.class)
    public void init() {
        while (true) {
            try {
                DelayTask task = delayQueue.take();
                processTask(task);
            } catch (InterruptedException e) {
                break;
            }
        }
    }

    /**
     * 加入到延时队列中
     *
     * @param task
     */
    public void put(DelayTask task) {
        delayQueue.put(task);
    }

    /**
     * 取消延时任务
     *
     * @param task
     * @return
     */
    public boolean remove(DelayTask task) {
        return delayQueue.remove(task);
    }

    /**
     * 取消延时任务
     *
     * @param id
     * @return
     */
    public boolean remove(String id) {
        return remove(new DelayTask(new DelayTaskElement(id), 0, TimeUnit.SECONDS));
    }

    /**
     * 执行延时任务
     *
     * @param task
     */
    private void processTask(DelayTask task) {
        //根据DelayTask中的DelayTaskElement的数据类型来处理相关逻辑
        // if (task.getDelayTaskElement() instanceof XXX) {}
        if (task.getDelayTaskElement() instanceof SessionAttributeExpireDelayTaskElement) {
            //延时清除Session中的属性值
            servletContext.removeAttribute(((SessionAttributeExpireDelayTaskElement)
                    task.getDelayTaskElement()).getKey());
        }
    }
}
