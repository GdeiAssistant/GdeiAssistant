package cn.gdeiassistant.Controller.ScheduleQuery.RestController;

import cn.gdeiassistant.Annotation.*;
import cn.gdeiassistant.Enum.Method.QueryMethodEnum;
import cn.gdeiassistant.Exception.CustomScheduleException.CountOverLimitException;
import cn.gdeiassistant.Exception.CustomScheduleException.GenerateScheduleException;
import cn.gdeiassistant.Exception.DatasourceException.MongodbNotConfiguredException;
import cn.gdeiassistant.Pojo.Entity.CustomSchedule;
import cn.gdeiassistant.Pojo.Entity.User;
import cn.gdeiassistant.Pojo.Result.DataJsonResult;
import cn.gdeiassistant.Pojo.Result.JsonResult;
import cn.gdeiassistant.Pojo.ScheduleQuery.ScheduleQueryResult;
import cn.gdeiassistant.Service.ScheduleQuery.ScheduleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
public class ScheduleQueryRestController {

    @Autowired
    private ScheduleService scheduleService;

    /**
     * 清空缓存课表信息
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/api/refreshschedule", method = RequestMethod.POST)
    public JsonResult RefreshGradeData(HttpServletRequest request) throws MongodbNotConfiguredException {
        String username = (String) request.getSession().getAttribute("username");
        scheduleService.ClearSchedule(username);
        return new JsonResult(true);
    }

    /**
     * 添加自定义课程
     *
     * @param request
     * @param customSchedule
     * @return
     * @throws GenerateScheduleException
     * @throws CountOverLimitException
     */
    @RequestMapping(value = "/api/customshedule", method = RequestMethod.POST)
    public JsonResult AddCustomSchedule(HttpServletRequest request, @Validated CustomSchedule customSchedule) throws GenerateScheduleException, CountOverLimitException, MongodbNotConfiguredException {
        String username = (String) request.getSession().getAttribute("username");
        scheduleService.AddCustomSchedule(username, customSchedule);
        return new JsonResult(true);
    }

    /**
     * 删除自定义课程
     *
     * @param request
     * @param id
     * @return
     */
    @RequestMapping(value = "/api/customschedule/id/{id}", method = RequestMethod.DELETE)
    public JsonResult DeleteCustomSchedule(HttpServletRequest request, @PathVariable String id) throws MongodbNotConfiguredException {
        String username = (String) request.getSession().getAttribute("username");
        scheduleService.DeleteCustomSchedule(username, id);
        return new JsonResult(true);
    }

    /**
     * 课表查询
     *
     * @param request
     * @param week
     * @param method
     * @return
     */
    @RequestMapping(value = "/api/schedulequery", method = RequestMethod.POST)
    @QueryLogPersistence
    @TrialData(value = "schedule", responseTime = "week")
    public DataJsonResult<ScheduleQueryResult> ScheduleQuery(HttpServletRequest request
            , Integer week, @RequestParam(value = "method", required = false
            , defaultValue = "0") QueryMethodEnum method) throws Exception {
        if ((week != null && (week < 0 || week > 20)) || method == null) {
            return new DataJsonResult<>(new JsonResult(false, "请求参数不合法"));
        }
        String username = (String) request.getSession().getAttribute("username");
        String password = (String) request.getSession().getAttribute("password");
        ScheduleQueryResult scheduleQueryResult = null;
        switch (method) {
            case CACHE_FIRST:
                //优先查询缓存数据
                scheduleQueryResult = scheduleService.QuerySchedule(request.getSession().getId()
                        , new User(username, password), week);
                break;

            case CACHE_ONLY:
                //只查询缓存数据
                scheduleQueryResult = scheduleService.QueryScheduleFromDocument(username, week);
                break;

            case QUERY_ONLY:
                scheduleQueryResult = scheduleService.QueryScheduleFromSystem(request.getSession().getId()
                        , new User(username, password), week);
                break;
        }
        return new DataJsonResult<>(true, scheduleQueryResult);
    }

    /**
     * 课表查询Rest接口
     *
     * @param request
     * @param token
     * @param week
     * @param method
     * @return
     */
    @RequestMapping(value = "/rest/schedulequery", method = RequestMethod.POST)
    @RestAuthentication
    @RestQueryLogPersistence
    @TrialData(value = "schedule", responseTime = "week")
    public DataJsonResult<ScheduleQueryResult> ScheduleQuery(HttpServletRequest request
            , @RequestParam("token") String token, Integer week
            , @RequestParam(name = "method", required = false
            , defaultValue = "0") QueryMethodEnum method) throws Exception {
        if ((week != null && (week < 0 || week > 20)) || method == null) {
            return new DataJsonResult<>(new JsonResult(false, "请求参数不合法"));
        }
        User user = (User) request.getAttribute("user");
        ScheduleQueryResult scheduleQueryResult = null;
        switch (method) {
            case CACHE_FIRST:
                //优先查询缓存数据
                scheduleQueryResult = scheduleService.QuerySchedule(request.getSession().getId(), user, week);
                break;

            case CACHE_ONLY:
                scheduleQueryResult = scheduleService.QueryScheduleFromDocument(user.getUsername(), week);
                break;

            case QUERY_ONLY:
                scheduleQueryResult = scheduleService.QueryScheduleFromSystem(request.getSession().getId(), user, week);
                break;
        }
        return new DataJsonResult<>(true, scheduleQueryResult);
    }
}
