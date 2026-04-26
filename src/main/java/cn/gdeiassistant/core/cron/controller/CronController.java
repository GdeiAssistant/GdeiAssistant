package cn.gdeiassistant.core.cron.controller;

import cn.gdeiassistant.common.annotation.RateLimit;
import cn.gdeiassistant.common.pojo.Result.JsonResult;
import cn.gdeiassistant.core.gradequery.service.GradeCronService;
import cn.gdeiassistant.core.schedulequery.service.ScheduleCronService;
import cn.gdeiassistant.core.information.service.SchoolNews.SchoolNewsCornService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@RestController
public class CronController {

    private static final Logger LOGGER = LoggerFactory.getLogger(CronController.class);

    @Autowired(required = false)
    private GradeCronService gradeCronService;

    @Autowired(required = false)
    private ScheduleCronService scheduleCronService;

    @Autowired(required = false)
    private SchoolNewsCornService schoolNewsCornService;

    @Value("${app.cron.secret}")
    private String cronSecret;

    /**
     * Validate the X-Cron-Secret header against the configured secret.
     * Returns true if authentication passes, false if a 403 response was sent.
     */
    private boolean authenticateCron(String secret, HttpServletResponse response) throws IOException {
        if (cronSecret == null || cronSecret.isEmpty() || secret == null || !secret.equals(cronSecret)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().print("{\"code\":403,\"message\":\"Forbidden: invalid or missing X-Cron-Secret\"}");
            response.getWriter().flush();
            return false;
        }
        return true;
    }

    private JsonResult runCronTask(String taskName, CronTask task) {
        try {
            task.run();
            return new JsonResult(true);
        } catch (Exception e) {
            LOGGER.error("Cron task failed: {}", taskName, e);
            return new JsonResult(false, "Cron task failed: " + taskName);
        }
    }

    @FunctionalInterface
    private interface CronTask {
        void run() throws Exception;
    }

    @RateLimit(maxRequests = 2, windowSeconds = 60)
    @PostMapping("/cron/grade")
    public JsonResult cacheGradeData(@RequestHeader(value = "X-Cron-Secret", required = false) String secret,
                                     HttpServletResponse response) throws IOException {
        if (!authenticateCron(secret, response)) {
            return null;
        }
        if (gradeCronService == null) {
            return new JsonResult(false);
        }
        return runCronTask("grade", gradeCronService::synchronizeGradeData);
    }

    @RateLimit(maxRequests = 2, windowSeconds = 60)
    @PostMapping("/cron/schedule")
    public JsonResult cacheScheduleData(@RequestHeader(value = "X-Cron-Secret", required = false) String secret,
                                        HttpServletResponse response) throws IOException {
        if (!authenticateCron(secret, response)) {
            return null;
        }
        if (scheduleCronService == null) {
            return new JsonResult(false);
        }
        return runCronTask("schedule", scheduleCronService::synchronizeScheduleData);
    }

    @RateLimit(maxRequests = 2, windowSeconds = 60)
    @PostMapping("/cron/news")
    public JsonResult collectNews(@RequestHeader(value = "X-Cron-Secret", required = false) String secret,
                                  HttpServletResponse response) throws IOException {
        if (!authenticateCron(secret, response)) {
            return null;
        }
        if (schoolNewsCornService == null) {
            return new JsonResult(false);
        }
        return runCronTask("news", schoolNewsCornService::collectNews);
    }

}
