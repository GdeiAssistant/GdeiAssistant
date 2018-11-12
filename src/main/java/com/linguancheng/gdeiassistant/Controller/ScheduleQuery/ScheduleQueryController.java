package com.linguancheng.gdeiassistant.Controller.ScheduleQuery;

import com.linguancheng.gdeiassistant.Annotation.QueryLog;
import com.linguancheng.gdeiassistant.Annotation.RestAuthentication;
import com.linguancheng.gdeiassistant.Annotation.RestQueryLog;
import com.linguancheng.gdeiassistant.Enum.Base.QueryMethodEnum;
import com.linguancheng.gdeiassistant.Pojo.Entity.User;
import com.linguancheng.gdeiassistant.Pojo.ScheduleQuery.ScheduleQueryJsonResult;
import com.linguancheng.gdeiassistant.Pojo.ScheduleQuery.ScheduleQueryResult;
import com.linguancheng.gdeiassistant.Service.ScheduleQuery.ScheduleQueryService;
import com.linguancheng.gdeiassistant.Service.UserLogin.UserLoginService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;

@Controller
public class ScheduleQueryController {

    @Autowired
    private UserLoginService userLoginService;

    @Autowired
    private ScheduleQueryService scheduleQueryService;

    @RequestMapping(value = "schedule", method = RequestMethod.GET)
    public ModelAndView ResolveSchedulePage() {
        return new ModelAndView("Schedule/schedule");
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
            , defaultValue = "0") QueryMethodEnum method) {
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
                    scheduleQueryResult = scheduleQueryService.GetScheduleDocument(user.getUsername(), week);
                    switch (scheduleQueryResult.getScheduleServiceResultEnum()) {
                        case SUCCESS:
                            //查询成功
                            result.setSuccess(true);
                            result.setScheduleList(scheduleQueryResult.getScheduleList());
                            result.setSelectedWeek(scheduleQueryResult.getSelectedWeek());
                            break;

                        case EMPTY_RESULT:
                            //缓存中没有数据
                        case SERVER_ERROR:
                            //缓存查询异常
                            scheduleQueryResult = scheduleQueryService
                                    .QueryScheduleData(request, user);
                            switch (scheduleQueryResult.getScheduleServiceResultEnum()) {
                                case SUCCESS:
                                    //查询成功
                                    result.setSuccess(true);
                                    if (week == null) {
                                        //无指定查询周数，则默认返回当前周数课表
                                        result.setScheduleList(scheduleQueryService
                                                .GetSpecifiedWeekSchedule(scheduleQueryResult.getScheduleList()
                                                        , scheduleQueryService.GetCurrentWeek()));
                                        result.setSelectedWeek(scheduleQueryService.GetCurrentWeek());
                                    } else if (week.equals(0)) {
                                        //若周数指定为0，则返回所有周数的课表
                                        result.setScheduleList(scheduleQueryResult.getScheduleList());
                                        result.setSelectedWeek(0);
                                    } else {
                                        //返回指定周数的课表
                                        result.setScheduleList(scheduleQueryService
                                                .GetSpecifiedWeekSchedule(scheduleQueryResult
                                                        .getScheduleList(), week));
                                        result.setSelectedWeek(week);
                                    }
                                    break;

                                case TIME_OUT:
                                    //网络连接超时
                                    result.setSuccess(false);
                                    result.setMessage("网络连接超时，请重试");
                                    break;

                                case PASSWORD_INCORRECT:
                                    //密码错误
                                    result.setSuccess(false);
                                    result.setMessage("密码已更新，请重新登录");
                                    break;

                                case TIMESTAMP_INVALID:
                                    //时间戳失效
                                    result.setSuccess(false);
                                    result.setMessage("时间戳校验失败，请尝试重新登录");
                                    break;

                                default:
                                case SERVER_ERROR:
                                    //教务系统异常
                                    result.setSuccess(false);
                                    result.setMessage("教务系统异常，请稍后再试");
                                    break;
                            }
                            break;
                    }
                    break;

                case CACHE_ONLY:
                    scheduleQueryResult = scheduleQueryService.GetScheduleDocument(user.getUsername(), week);
                    switch (scheduleQueryResult.getScheduleServiceResultEnum()) {
                        case SUCCESS:
                            //查询成功
                            result.setSuccess(true);
                            result.setScheduleList(scheduleQueryResult.getScheduleList());
                            result.setSelectedWeek(scheduleQueryResult.getSelectedWeek());
                            break;

                        case EMPTY_RESULT:
                            //缓存中没有数据
                            result.setSuccess(false);
                            result.setMessage("用户课表信息未同步，请稍后再试");
                            break;

                        case SERVER_ERROR:
                            //缓存查询异常
                            result.setSuccess(false);
                            result.setMessage("教务系统异常，请稍后再试");
                            break;
                    }
                    break;

                case QUERY_ONLY:
                    scheduleQueryResult = scheduleQueryService
                            .QueryScheduleData(request, user);
                    switch (scheduleQueryResult.getScheduleServiceResultEnum()) {
                        case SUCCESS:
                            //查询成功
                            result.setSuccess(true);
                            if (week == null) {
                                //无指定查询周数，则默认返回当前周数课表
                                result.setScheduleList(scheduleQueryService
                                        .GetSpecifiedWeekSchedule(scheduleQueryResult.getScheduleList()
                                                , scheduleQueryService.GetCurrentWeek()));
                                result.setSelectedWeek(scheduleQueryService.GetCurrentWeek());
                            } else if (week.equals(0)) {
                                //若周数指定为0，则返回所有周数的课表
                                result.setScheduleList(scheduleQueryResult.getScheduleList());
                                result.setSelectedWeek(0);
                            } else {
                                //返回指定周数的课表
                                result.setScheduleList(scheduleQueryService
                                        .GetSpecifiedWeekSchedule(scheduleQueryResult
                                                .getScheduleList(), week));
                                result.setSelectedWeek(week);
                            }
                            break;

                        case TIME_OUT:
                            //网络连接超时
                            result.setSuccess(false);
                            result.setMessage("网络连接超时，请重试");
                            break;

                        case PASSWORD_INCORRECT:
                            //密码错误
                            result.setSuccess(false);
                            result.setMessage("密码已更新，请重新登录");
                            break;

                        case TIMESTAMP_INVALID:
                            //时间戳失效
                            result.setSuccess(false);
                            result.setMessage("时间戳校验失败，请尝试重新登录");
                            break;

                        default:
                        case SERVER_ERROR:
                            //教务系统异常
                            result.setSuccess(false);
                            result.setMessage("教务系统异常，请稍后再试");
                            break;
                    }
                    break;
            }
        }
        return result;
    }

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
            , defaultValue = "0") QueryMethodEnum method) {
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
                scheduleQueryResult = scheduleQueryService.GetScheduleDocument(username, week);
                switch (scheduleQueryResult.getScheduleServiceResultEnum()) {
                    case SUCCESS:
                        //查询成功
                        result.setScheduleList(scheduleQueryResult.getScheduleList());
                        result.setSelectedWeek(scheduleQueryResult.getSelectedWeek());
                        break;

                    case EMPTY_RESULT:
                        //缓存中没有数据
                    case SERVER_ERROR:
                        //缓存查询异常
                        scheduleQueryResult = scheduleQueryService.QueryScheduleData(request
                                , new User(username, password));
                        switch (scheduleQueryResult.getScheduleServiceResultEnum()) {
                            case SUCCESS:
                                //查询成功
                                result.setSuccess(true);
                                if (week == null) {
                                    //无指定查询周数，则默认返回当前周数课表
                                    result.setScheduleList(scheduleQueryService
                                            .GetSpecifiedWeekSchedule(scheduleQueryResult.getScheduleList()
                                                    , scheduleQueryService.GetCurrentWeek()));
                                    result.setSelectedWeek(scheduleQueryService.GetCurrentWeek());
                                } else if (week.equals(0)) {
                                    //若周数指定为0，则返回所有周数的课表
                                    result.setScheduleList(scheduleQueryResult.getScheduleList());
                                    result.setSelectedWeek(0);
                                } else {
                                    //返回指定周数的课表
                                    result.setScheduleList(scheduleQueryService
                                            .GetSpecifiedWeekSchedule(scheduleQueryResult
                                                    .getScheduleList(), week));
                                    result.setSelectedWeek(week);
                                }
                                break;

                            case TIMESTAMP_INVALID:
                                //时间戳失效
                                result.setSuccess(false);
                                result.setMessage("时间戳校验失败，请尝试重新登录");
                                break;

                            case TIME_OUT:
                                //网络连接超时
                                result.setSuccess(false);
                                result.setMessage("网络连接超时，请重试");
                                break;

                            case PASSWORD_INCORRECT:
                                //密码错误
                                result.setSuccess(false);
                                result.setMessage("密码已更新，请重新登录");
                                break;

                            case SERVER_ERROR:
                            default:
                                //教务系统异常
                                result.setSuccess(false);
                                result.setMessage("教务系统异常，请稍后再试");
                                break;
                        }
                        break;
                }
                break;

            case CACHE_ONLY:
                scheduleQueryResult = scheduleQueryService.GetScheduleDocument(username, week);
                switch (scheduleQueryResult.getScheduleServiceResultEnum()) {
                    case SUCCESS:
                        //查询成功
                        result.setSuccess(true);
                        result.setScheduleList(scheduleQueryResult.getScheduleList());
                        result.setSelectedWeek(scheduleQueryResult.getSelectedWeek());
                        break;

                    case EMPTY_RESULT:
                        //缓存中没有数据
                        result.setSuccess(false);
                        result.setMessage("用户课表信息未同步，请稍后再试");
                        break;

                    case SERVER_ERROR:
                        //缓存查询异常
                        result.setSuccess(false);
                        result.setMessage("教务系统异常，请稍后再试");
                        break;
                }
                break;

            case QUERY_ONLY:
                scheduleQueryResult = scheduleQueryService.QueryScheduleData(request
                        , new User(username, password));
                switch (scheduleQueryResult.getScheduleServiceResultEnum()) {
                    case SUCCESS:
                        //查询成功
                        result.setSuccess(true);
                        if (week == null) {
                            //无指定查询周数，则默认返回当前周数课表
                            result.setScheduleList(scheduleQueryService
                                    .GetSpecifiedWeekSchedule(scheduleQueryResult.getScheduleList()
                                            , scheduleQueryService.GetCurrentWeek()));
                            result.setSelectedWeek(scheduleQueryService.GetCurrentWeek());
                        } else if (week.equals(0)) {
                            //若周数指定为0，则返回所有周数的课表
                            result.setScheduleList(scheduleQueryResult.getScheduleList());
                            result.setSelectedWeek(0);
                        } else {
                            //返回指定周数的课表
                            result.setScheduleList(scheduleQueryService
                                    .GetSpecifiedWeekSchedule(scheduleQueryResult
                                            .getScheduleList(), week));
                            result.setSelectedWeek(week);
                        }
                        break;

                    case TIME_OUT:
                        //网络连接超时
                        result.setSuccess(false);
                        result.setMessage("网络连接超时，请重试");
                        break;

                    case TIMESTAMP_INVALID:
                        //时间戳失效
                        result.setSuccess(false);
                        result.setMessage("时间戳校验失败，请尝试重新登录");
                        break;

                    case PASSWORD_INCORRECT:
                        //密码错误
                        result.setSuccess(false);
                        result.setMessage("密码已更新，请重新登录");
                        break;

                    case SERVER_ERROR:
                    default:
                        //教务系统异常
                        result.setSuccess(false);
                        result.setMessage("教务系统异常，请稍后再试");
                        break;
                }
                break;
        }
        return result;
    }
}
