package com.linguancheng.gdeiassistant.Controller.ScheduleQuery;

import com.linguancheng.gdeiassistant.Annotation.QueryLogPersistence;
import com.linguancheng.gdeiassistant.Annotation.RestAuthentication;
import com.linguancheng.gdeiassistant.Annotation.RestQueryLogPersistence;
import com.linguancheng.gdeiassistant.Enum.Base.QueryMethodEnum;
import com.linguancheng.gdeiassistant.Exception.CustomScheduleException.CountOverLimitException;
import com.linguancheng.gdeiassistant.Exception.CustomScheduleException.GenerateScheduleException;
import com.linguancheng.gdeiassistant.Pojo.Entity.CustomSchedule;
import com.linguancheng.gdeiassistant.Pojo.Entity.User;
import com.linguancheng.gdeiassistant.Pojo.Result.DataJsonResult;
import com.linguancheng.gdeiassistant.Pojo.Result.JsonResult;
import com.linguancheng.gdeiassistant.Pojo.ScheduleQuery.ScheduleQueryResult;
import com.linguancheng.gdeiassistant.Service.ScheduleQuery.ScheduleQueryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
public class ScheduleQueryRestController {

    @Autowired
    private ScheduleQueryService scheduleQueryService;

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
    public JsonResult AddCustomSchedule(HttpServletRequest request, @Validated CustomSchedule customSchedule) throws GenerateScheduleException, CountOverLimitException {
        String username = (String) request.getSession().getAttribute("username");
        scheduleQueryService.AddCustomSchedule(username, customSchedule);
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
    public JsonResult DeleteCustomSchedule(HttpServletRequest request, @PathVariable String id) {
        String username = (String) request.getSession().getAttribute("username");
        scheduleQueryService.DeleteCustomSchedule(username, id);
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
                scheduleQueryResult = scheduleQueryService.QuerySchedule(request.getSession().getId()
                        , new User(username, password), week);
                break;

            case CACHE_ONLY:
                //只查询缓存数据
                scheduleQueryResult = scheduleQueryService.QueryScheduleFromDocument(username, week);
                break;

            case QUERY_ONLY:
                scheduleQueryResult = scheduleQueryService.QueryScheduleFromSystem(request.getSession().getId()
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
                scheduleQueryResult = scheduleQueryService.QuerySchedule(request.getSession().getId(), user, week);
                break;

            case CACHE_ONLY:
                scheduleQueryResult = scheduleQueryService.QueryScheduleFromDocument(user.getUsername(), week);
                break;

            case QUERY_ONLY:
                scheduleQueryResult = scheduleQueryService.QueryScheduleFromSystem(request.getSession().getId(), user, week);
                break;
        }
        return new DataJsonResult<>(true, scheduleQueryResult);
    }
}
