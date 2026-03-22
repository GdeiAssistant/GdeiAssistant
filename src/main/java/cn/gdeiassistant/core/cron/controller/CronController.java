package cn.gdeiassistant.core.cron.controller;

import cn.gdeiassistant.common.annotation.RateLimit;
import cn.gdeiassistant.common.pojo.Result.JsonResult;
import cn.gdeiassistant.core.gradequery.service.GradeCronService;
import cn.gdeiassistant.core.schedulequery.service.ScheduleCronService;
import cn.gdeiassistant.core.information.service.SchoolNews.SchoolNewsCornService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

@RestController
public class CronController {

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

    // TODO: These cron endpoints are fire-and-forget — they return success immediately
    // while the underlying service methods may execute asynchronously. The response
    // does not reflect whether the task actually completed successfully.

    @RateLimit(maxRequests = 2, windowSeconds = 60)
    @RequestMapping(value = "/cron/grade", method = RequestMethod.GET)
    public JsonResult cacheGradeData(@RequestHeader(value = "X-Cron-Secret", required = false) String secret,
                                     HttpServletResponse response) throws IOException {
        if (!authenticateCron(secret, response)) {
            return null;
        }
        if (gradeCronService != null) {
            gradeCronService.synchronizeGradeData();
            return new JsonResult(true);
        }
        return new JsonResult(false);
    }

    @RateLimit(maxRequests = 2, windowSeconds = 60)
    @RequestMapping(value = "/cron/schedule", method = RequestMethod.GET)
    public JsonResult cacheScheduleData(@RequestHeader(value = "X-Cron-Secret", required = false) String secret,
                                        HttpServletResponse response) throws IOException {
        if (!authenticateCron(secret, response)) {
            return null;
        }
        if (scheduleCronService != null) {
            scheduleCronService.synchronizeScheduleData();
            return new JsonResult(true);
        }
        return new JsonResult(false);
    }

    @RateLimit(maxRequests = 2, windowSeconds = 60)
    @RequestMapping(value = "/cron/news", method = RequestMethod.GET)
    public JsonResult collectNews(@RequestHeader(value = "X-Cron-Secret", required = false) String secret,
                                  HttpServletResponse response) throws InterruptedException, ExecutionException, IOException {
        if (!authenticateCron(secret, response)) {
            return null;
        }
        if (schoolNewsCornService != null) {
            schoolNewsCornService.collectNews();
            return new JsonResult(true);
        }
        return new JsonResult(false);
    }

}
