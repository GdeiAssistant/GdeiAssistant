package cn.gdeiassistant.Controller.Cron.RestController;

import cn.gdeiassistant.Pojo.Result.JsonResult;
import cn.gdeiassistant.Service.AcademicAffairs.GradeQuery.GradeCronService;
import cn.gdeiassistant.Service.AcademicAffairs.ScheduleQuery.ScheduleCronService;
import cn.gdeiassistant.Service.Information.SchoolNews.SchoolNewsCornService;
import cn.gdeiassistant.Service.ThirdParty.Wechat.WechatCronService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

@RestController
public class CronRestController {

    @Autowired(required = false)
    private GradeCronService gradeCronService;

    @Autowired(required = false)
    private ScheduleCronService scheduleCronService;

    @Autowired(required = false)
    private WechatCronService wechatCronService;

    @Autowired(required = false)
    private SchoolNewsCornService schoolNewsCornService;

    @RequestMapping(value = "/cron/grade", method = RequestMethod.GET)
    public JsonResult CacheGradeData() {
        if (gradeCronService != null) {
            gradeCronService.SynchronizeGradeData();
            return new JsonResult(true);
        }
        return new JsonResult(false);
    }

    @RequestMapping(value = "/cron/schedule", method = RequestMethod.GET)
    public JsonResult CacheScheduleData() {
        if (scheduleCronService != null) {
            scheduleCronService.SynchronizeScheduleData();
            return new JsonResult(true);
        }
        return new JsonResult(false);
    }

    @RequestMapping(value = "/cron/wechat/reading", method = RequestMethod.GET)
    public JsonResult SyncWechatReadingData() {
        if (wechatCronService != null) {
            wechatCronService.SyncWechatReadingItem();
            return new JsonResult(true);
        }
        return new JsonResult(false);
    }

    @RequestMapping(value = "/cron/wechat/account", method = RequestMethod.GET)
    public JsonResult UpdateWechatAccountData() {
        if (wechatCronService != null) {
            wechatCronService.UpdateAccountData();
            return new JsonResult(true);
        }
        return new JsonResult(false);
    }

    @RequestMapping(value = "/cron/news", method = RequestMethod.GET)
    public JsonResult CollectNews() throws InterruptedException, ExecutionException, IOException {
        if (schoolNewsCornService != null) {
            schoolNewsCornService.CollectNews();
            return new JsonResult(true);
        }
        return new JsonResult(false);
    }

}
