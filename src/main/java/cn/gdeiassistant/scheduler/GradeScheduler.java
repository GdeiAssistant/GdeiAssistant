package cn.gdeiassistant.scheduler;

import cn.gdeiassistant.core.gradequery.service.GradeCronService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 成绩缓存定时触发：仅负责按固定间隔调用 GradeCronService.synchronizeGradeData()，不包含业务逻辑。
 */
@Component
@Profile("production")
public class GradeScheduler {

    @Autowired(required = false)
    private GradeCronService gradeCronService;

    @Scheduled(fixedDelay = 7200000)
    public void synchronizeGradeData() {
        if (gradeCronService != null) {
            gradeCronService.synchronizeGradeData();
        }
    }
}
