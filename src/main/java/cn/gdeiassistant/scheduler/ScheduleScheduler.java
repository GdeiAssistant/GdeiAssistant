package cn.gdeiassistant.scheduler;

import cn.gdeiassistant.core.schedulequery.service.ScheduleCronService;
import org.springframework.beans.factory.ObjectProvider;
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

    private final ScheduleCronService scheduleCronService;

        public ScheduleScheduler() {
        this.scheduleCronService = null;
    }

    public ScheduleScheduler(ScheduleCronService scheduleCronService) {
        this.scheduleCronService = scheduleCronService;
    }

    @Autowired
    public ScheduleScheduler(ObjectProvider<ScheduleCronService> scheduleCronService) {
        this(scheduleCronService.getIfAvailable());
    }

    @Scheduled(fixedDelay = 7200000)
    public void synchronizeScheduleData() {
        if (scheduleCronService != null) {
            scheduleCronService.synchronizeScheduleData();
        }
    }
}
