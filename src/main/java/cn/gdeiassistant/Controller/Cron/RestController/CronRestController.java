package cn.gdeiassistant.Controller.Cron.RestController;

import cn.gdeiassistant.Pojo.Result.JsonResult;
import cn.gdeiassistant.Service.GradeQuery.GradeCronService;
import cn.gdeiassistant.Service.ScheduleQuery.ScheduleCronService;
import cn.gdeiassistant.Service.SchoolNews.SchoolNewsCornService;
import cn.gdeiassistant.Service.Wechat.WechatCronService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

@RestController
public class CronRestController {

    @Autowired
    private GradeCronService gradeCronService;

    @Autowired
    private ScheduleCronService scheduleCronService;

    @Autowired
    private WechatCronService wechatCronService;

    @Autowired
    private SchoolNewsCornService schoolNewsCornService;

    @RequestMapping(value = "/cron/grade", method = RequestMethod.GET)
    public JsonResult CacheGradeData(HttpServletRequest request) {
        gradeCronService.SynchronizeGradeData();
        return new JsonResult(true);
    }

    @RequestMapping(value = "/cron/schedule", method = RequestMethod.GET)
    public JsonResult CacheScheduleData(HttpServletRequest request) {
        scheduleCronService.SynchronizeScheduleData();
        return new JsonResult(true);
    }

    @RequestMapping(value = "/cron/wechat/reading", method = RequestMethod.GET)
    public JsonResult SyncWechatReadingData(HttpServletRequest request) {
        wechatCronService.SyncWechatReadingItem();
        return new JsonResult(true);
    }

    @RequestMapping(value = "/cron/wechat/account", method = RequestMethod.GET)
    public JsonResult UpdateWechatAccountData(HttpServletRequest request) {
        wechatCronService.UpdateAccountData();
        return new JsonResult(true);
    }

    @RequestMapping(value = "/cron/news", method = RequestMethod.GET)
    public JsonResult CollectNews(HttpServletRequest request) throws InterruptedException, ExecutionException, IOException {
        schoolNewsCornService.CollectNews();
        return new JsonResult(true);
    }

}
