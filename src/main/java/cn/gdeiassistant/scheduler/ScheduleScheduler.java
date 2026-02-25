package cn.gdeiassistant.scheduler;

import cn.gdeiassistant.core.schedulequery.service.ScheduleCronService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 课表缓存定时触发：仅负责按固定间隔调用 ScheduleCronService.SynchronizeScheduleData()，不包含业务逻辑。
 */
@Component
@Profile("production")
public class ScheduleScheduler {

    @Autowired(required = false)
    private ScheduleCronService scheduleCronService;

    @Scheduled(fixedDelay = 7200000)
    public void synchronizeScheduleData() {
        if (scheduleCronService != null) {
            scheduleCronService.synchronizeScheduleData();
        }
    }
}
