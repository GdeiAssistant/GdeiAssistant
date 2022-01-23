package cn.gdeiassistant.Tools.SpringUtils;

import cn.gdeiassistant.Enum.Module.ModuleEnum;
import cn.gdeiassistant.Pojo.DelayTask.DelayTask;
import cn.gdeiassistant.Pojo.DelayTask.SessionAttributeExpireDelayTaskElement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.servlet.ServletContext;
import java.util.concurrent.TimeUnit;

@Component
public class RedisDaoUtils {

    @Autowired(required = false)
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private ModuleUtils moduleUtils;

    @Autowired
    private ServletContext servletContext;

    @Autowired
    private DelayTaskUtils delayTaskUtils;

    public <T> T get(String key) {
        if (moduleUtils.CheckModuleState(ModuleEnum.REDIS)) {
            return (T) redisTemplate.opsForValue().get(key);
        }
        return (T) servletContext.getAttribute(key);
    }

    public <E> void set(String key, E object) {
        if (moduleUtils.CheckModuleState(ModuleEnum.REDIS)) {
            redisTemplate.opsForValue().set(key, object);
        } else {
            servletContext.setAttribute(key, object);
        }
    }

    public void delete(String key) {
        if (moduleUtils.CheckModuleState(ModuleEnum.REDIS)) {
            redisTemplate.delete(key);
        } else {
            servletContext.removeAttribute(key);
        }
    }

    public void expire(String key, long timeout, TimeUnit unit) {
        if (moduleUtils.CheckModuleState(ModuleEnum.REDIS)) {
            redisTemplate.expire(key, timeout, unit);
        } else {
            delayTaskUtils.put(new DelayTask(new SessionAttributeExpireDelayTaskElement(key)
                    , timeout, unit));
        }
    }
}
