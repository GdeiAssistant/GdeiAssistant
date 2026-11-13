package cn.gdeiassistant.scheduler;

import cn.gdeiassistant.core.information.service.schoolnews.SchoolNewsCronService;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

/**
 * 校园新闻定时触发：仅负责按 cron 表达式调用 SchoolNewsCronService.collectNews()，不包含业务逻辑。
 */
@Component
@Profile("production")
public class NewsScheduler {

    private final SchoolNewsCronService schoolNewsCronService;

        public NewsScheduler() {
        this.schoolNewsCronService = null;
    }

    public NewsScheduler(SchoolNewsCronService schoolNewsCronService) {
        this.schoolNewsCronService = schoolNewsCronService;
    }

    @Autowired
    public NewsScheduler(ObjectProvider<SchoolNewsCronService> schoolNewsCronService) {
        this(schoolNewsCronService.getIfAvailable());
    }

    @Scheduled(cron = "0 0 0,9,18 * * ?")
    public void collectNews() throws IOException, ExecutionException, InterruptedException {
        if (schoolNewsCronService != null) {
            schoolNewsCronService.collectNews();
        }
    }
}
