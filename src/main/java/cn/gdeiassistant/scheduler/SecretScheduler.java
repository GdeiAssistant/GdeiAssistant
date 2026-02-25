package cn.gdeiassistant.scheduler;

import cn.gdeiassistant.core.secret.service.SecretService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 树洞定时清理触发：仅负责按固定间隔调用 SecretService.DeleteTimerSecretInfos()，不包含业务逻辑。
 */
@Component
public class SecretScheduler {

    @Autowired(required = false)
    private SecretService secretService;

    @Scheduled(fixedDelay = 300000)
    public void deleteTimerSecretInfos() throws Exception {
        if (secretService != null) {
            secretService.deleteTimerSecretInfos();
        }
    }
}
