package com.linguancheng.gdeiassistant.Controller.ScheduleQuery;

import com.linguancheng.gdeiassistant.Annotation.QueryLog;
import com.linguancheng.gdeiassistant.Annotation.RestQueryLog;
import com.linguancheng.gdeiassistant.Enum.Query.QueryMethodEnum;
import com.linguancheng.gdeiassistant.Pojo.Entity.User;
import com.linguancheng.gdeiassistant.Pojo.ScheduleQuery.ScheduleQueryJsonResult;
import com.linguancheng.gdeiassistant.Pojo.ScheduleQuery.ScheduleQueryResult;
import com.linguancheng.gdeiassistant.Pojo.UserLogin.UserLoginResult;
import com.linguancheng.gdeiassistant.Service.ScheduleQuery.ScheduleQueryService;
import com.linguancheng.gdeiassistant.Service.UserLogin.UserLoginService;
import com.linguancheng.gdeiassistant.ValidGroup.User.UserLoginValidGroup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
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
     * @param user
     * @param week
     * @param method
     * @param timestamp
     * @param bindingResult
     * @return
     */
    @RequestMapping(value = "/rest/schedulequery", method = RequestMethod.POST)
    @RestQueryLog
    @ResponseBody
    public ScheduleQueryJsonResult ScheduleQuery(HttpServletRequest request
            , @ModelAttribute("user") @Validated(value = UserLoginValidGroup.class) User user
            , BindingResult bindingResult, Integer week, Long timestamp
            , @RequestParam(name = "method", required = false
            , defaultValue = "0") QueryMethodEnum method) {
        ScheduleQueryJsonResult result = new ScheduleQueryJsonResult();
        if (bindingResult.hasErrors()) {
            result.setSuccess(false);
            result.setErrorMessage("API接口已更新，请更新应用至最新版本");
        } else if ((week != null && (week < 0 || week > 20)) || method == null) {
            result.setSuccess(false);
            result.setErrorMessage("请求参数不合法");
        } else {
            //校验用户账号身份
            UserLoginResult userLoginResult = userLoginService.UserLogin(request
                    , user, true);
            ScheduleQueryResult scheduleQueryResult = null;
            switch (userLoginResult.getLoginResultEnum()) {
                case LOGIN_SUCCESS:
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
                                    scheduleQueryResult = scheduleQueryService.QueryScheduleData(request, user, timestamp);
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
                                            result.setErrorMessage("网络连接超时，请重试");
                                            break;

                                        case PASSWORD_INCORRECT:
                                            //密码错误
                                            result.setSuccess(false);
                                            result.setErrorMessage("密码已更新，请重新登录");
                                            break;

                                        case TIMESTAMP_INVALID:
                                            //时间戳失效
                                            result.setSuccess(false);
                                            result.setErrorMessage("时间戳校验失败，请尝试重新登录");
                                            break;

                                        default:
                                        case SERVER_ERROR:
                                            //教务系统异常
                                            result.setSuccess(false);
                                            result.setErrorMessage("教务系统异常，请稍后再试");
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
                                    result.setErrorMessage("用户课表信息未同步，请稍后再试");
                                    break;

                                case SERVER_ERROR:
                                    //缓存查询异常
                                    result.setSuccess(false);
                                    result.setErrorMessage("教务系统异常，请稍后再试");
                                    break;
                            }
                            break;

                        case QUERY_ONLY:
                            scheduleQueryResult = scheduleQueryService.QueryScheduleData(request, user, timestamp);
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
                                    result.setErrorMessage("网络连接超时，请重试");
                                    break;

                                case PASSWORD_INCORRECT:
                                    //密码错误
                                    result.setSuccess(false);
                                    result.setErrorMessage("密码已更新，请重新登录");
                                    break;

                                case TIMESTAMP_INVALID:
                                    //时间戳失效
                                    result.setSuccess(false);
                                    result.setErrorMessage("时间戳校验失败，请尝试重新登录");
                                    break;

                                default:
                                case SERVER_ERROR:
                                    //教务系统异常
                                    result.setSuccess(false);
                                    result.setErrorMessage("教务系统异常，请稍后再试");
                                    break;
                            }
                            break;
                    }
                    break;

                case TIME_OUT:
                    //网络连接超时
                    result.setSuccess(false);
                    result.setErrorMessage("网络连接超时，请重试");
                    break;

                case SERVER_ERROR:
                    //教务系统异常
                    result.setSuccess(false);
                    result.setErrorMessage("教务系统异常，请稍后再试");
                    break;

                case PASSWORD_ERROR:
                    //密码错误
                    result.setSuccess(false);
                    result.setErrorMessage("密码已更新，请重新登录");
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
            result.setErrorMessage("查询的周数不合法");
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
                                , new User(username, password), null);
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
                                result.setErrorMessage("时间戳校验失败，请尝试重新登录");
                                break;

                            case TIME_OUT:
                                //网络连接超时
                                result.setSuccess(false);
                                result.setErrorMessage("网络连接超时，请重试");
                                break;

                            case PASSWORD_INCORRECT:
                                //密码错误
                                result.setSuccess(false);
                                result.setErrorMessage("密码已更新，请重新登录");
                                break;

                            case SERVER_ERROR:
                            default:
                                //教务系统异常
                                result.setSuccess(false);
                                result.setErrorMessage("教务系统异常，请稍后再试");
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
                        result.setErrorMessage("用户课表信息未同步，请稍后再试");
                        break;

                    case SERVER_ERROR:
                        //缓存查询异常
                        result.setSuccess(false);
                        result.setErrorMessage("教务系统异常，请稍后再试");
                        break;
                }
                break;

            case QUERY_ONLY:
                scheduleQueryResult = scheduleQueryService.QueryScheduleData(request
                        , new User(username, password), null);
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
                        result.setErrorMessage("网络连接超时，请重试");
                        break;

                    case TIMESTAMP_INVALID:
                        //时间戳失效
                        result.setSuccess(false);
                        result.setErrorMessage("时间戳校验失败，请尝试重新登录");
                        break;

                    case PASSWORD_INCORRECT:
                        //密码错误
                        result.setSuccess(false);
                        result.setErrorMessage("密码已更新，请重新登录");
                        break;

                    case SERVER_ERROR:
                    default:
                        //教务系统异常
                        result.setSuccess(false);
                        result.setErrorMessage("教务系统异常，请稍后再试");
                        break;
                }
                break;
        }
        return result;
    }
}
