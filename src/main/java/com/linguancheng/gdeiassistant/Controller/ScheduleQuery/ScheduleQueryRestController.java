package com.linguancheng.gdeiassistant.Controller.ScheduleQuery;

import com.linguancheng.gdeiassistant.Annotation.QueryLog;
import com.linguancheng.gdeiassistant.Annotation.RestAuthentication;
import com.linguancheng.gdeiassistant.Annotation.RestQueryLog;
import com.linguancheng.gdeiassistant.Enum.Base.QueryMethodEnum;
import com.linguancheng.gdeiassistant.Pojo.Entity.User;
import com.linguancheng.gdeiassistant.Pojo.ScheduleQuery.ScheduleQueryJsonResult;
import com.linguancheng.gdeiassistant.Pojo.ScheduleQuery.ScheduleQueryResult;
import com.linguancheng.gdeiassistant.Service.ScheduleQuery.ScheduleQueryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;

@RestController
public class ScheduleQueryRestController {

    @Autowired
    private ScheduleQueryService scheduleQueryService;

    /**
     * 课表查询
     *
     * @param request
     * @param week
     * @param method
     * @return
     */
    @RequestMapping(value = "/schedulequery", method = RequestMethod.POST)
    @QueryLog
    @ResponseBody
    public ScheduleQueryJsonResult ScheduleQuery(HttpServletRequest request
            , Integer week, @RequestParam(value = "method", required = false
            , defaultValue = "0") QueryMethodEnum method) throws Exception {
        ScheduleQueryJsonResult result = new ScheduleQueryJsonResult();
        if ((week != null && (week < 0 || week > 20)) || method == null) {
            result.setSuccess(false);
            result.setMessage("查询的周数不合法");
            return result;
        }
        String username = (String) request.getSession().getAttribute("username");
        String password = (String) request.getSession().getAttribute("password");
        ScheduleQueryResult scheduleQueryResult = null;
        switch (method) {
            case CACHE_FIRST:
                //优先查询缓存数据
                scheduleQueryResult = scheduleQueryService.QuerySchedule(request.getSession().getId()
                        , new User(username, password), week);
                result.setSuccess(true);
                result.setScheduleList(scheduleQueryResult.getScheduleList());
                result.setWeek(scheduleQueryResult.getWeek());
                break;

            case CACHE_ONLY:
                //只查询缓存数据
                scheduleQueryResult = scheduleQueryService.QueryScheduleFromDocument(username, week);
                result.setSuccess(true);
                result.setScheduleList(scheduleQueryResult == null ? new ArrayList<>() : scheduleQueryResult.getScheduleList());
                result.setWeek(scheduleQueryResult == null ? 0 : scheduleQueryResult.getWeek());
                break;

            case QUERY_ONLY:
                scheduleQueryResult = scheduleQueryService.QueryScheduleFromSystem(request.getSession().getId()
                        , new User(username, password), week);
                result.setSuccess(true);
                result.setScheduleList(scheduleQueryResult.getScheduleList());
                result.setWeek(scheduleQueryResult.getWeek());
                result.setSuccess(true);
                break;
        }
        return result;
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
    @RestQueryLog
    @ResponseBody
    public ScheduleQueryJsonResult ScheduleQuery(HttpServletRequest request
            , @RequestParam("token") String token, Integer week
            , @RequestParam(name = "method", required = false
            , defaultValue = "0") QueryMethodEnum method) throws Exception {
        ScheduleQueryJsonResult result = new ScheduleQueryJsonResult();
        if ((week != null && (week < 0 || week > 20)) || method == null) {
            result.setSuccess(false);
            result.setMessage("请求参数不合法");
        } else {
            User user = (User) request.getAttribute("user");
            ScheduleQueryResult scheduleQueryResult = null;
            switch (method) {
                case CACHE_FIRST:
                    //优先查询缓存数据
                    scheduleQueryResult = scheduleQueryService.QuerySchedule(request.getSession().getId(), user, week);
                    result.setSuccess(true);
                    result.setScheduleList(scheduleQueryResult.getScheduleList());
                    result.setWeek(scheduleQueryResult.getWeek());
                    break;

                case CACHE_ONLY:
                    scheduleQueryResult = scheduleQueryService.QueryScheduleFromDocument(user.getUsername(), week);
                    result.setSuccess(true);
                    result.setScheduleList(scheduleQueryResult == null ? new ArrayList<>() : scheduleQueryResult.getScheduleList());
                    result.setWeek(scheduleQueryResult == null ? 0 : scheduleQueryResult.getWeek());
                    break;

                case QUERY_ONLY:
                    scheduleQueryResult = scheduleQueryService.QueryScheduleFromSystem(request.getSession().getId(), user, week);
                    result.setSuccess(true);
                    result.setScheduleList(scheduleQueryResult.getScheduleList());
                    result.setWeek(scheduleQueryResult.getWeek());
                    break;
            }
        }
        return result;
    }
}
