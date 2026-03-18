package cn.gdeiassistant.core.cron.controller;

import cn.gdeiassistant.common.pojo.Result.JsonResult;
import cn.gdeiassistant.core.gradequery.service.GradeCronService;
import cn.gdeiassistant.core.schedulequery.service.ScheduleCronService;
import cn.gdeiassistant.core.information.service.SchoolNews.SchoolNewsCornService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

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

    @RequestMapping(value = "/cron/grade", method = RequestMethod.GET)
    public JsonResult cacheGradeData() {
        if (gradeCronService != null) {
            gradeCronService.synchronizeGradeData();
            return new JsonResult(true);
        }
        return new JsonResult(false);
    }

    @RequestMapping(value = "/cron/schedule", method = RequestMethod.GET)
    public JsonResult cacheScheduleData() {
        if (scheduleCronService != null) {
            scheduleCronService.synchronizeScheduleData();
            return new JsonResult(true);
        }
        return new JsonResult(false);
    }

    @RequestMapping(value = "/cron/news", method = RequestMethod.GET)
    public JsonResult collectNews() throws InterruptedException, ExecutionException, IOException {
        if (schoolNewsCornService != null) {
            schoolNewsCornService.collectNews();
            return new JsonResult(true);
        }
        return new JsonResult(false);
    }

}
