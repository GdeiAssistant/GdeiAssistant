package com.linguancheng.gdeiassistant.Controller.ScheduleQuery;

import com.linguancheng.gdeiassistant.Annotation.QueryLog;
import com.linguancheng.gdeiassistant.Annotation.RestQueryLog;
import com.linguancheng.gdeiassistant.Enum.Base.LoginResultEnum;
import com.linguancheng.gdeiassistant.Enum.Base.ServiceResultEnum;
import com.linguancheng.gdeiassistant.Pojo.Document.ScheduleDocument;
import com.linguancheng.gdeiassistant.Pojo.Entity.Schedule;
import com.linguancheng.gdeiassistant.Pojo.Entity.User;
import com.linguancheng.gdeiassistant.Pojo.Result.BaseResult;
import com.linguancheng.gdeiassistant.Pojo.ScheduleQuery.ScheduleQueryJsonResult;
import com.linguancheng.gdeiassistant.Service.ScheduleQuery.ScheduleCacheService;
import com.linguancheng.gdeiassistant.Service.ScheduleQuery.ScheduleQueryService;
import com.linguancheng.gdeiassistant.Service.UserLogin.UserLoginService;
import com.linguancheng.gdeiassistant.ValidGroup.User.ServiceQueryValidGroup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Controller
public class ScheduleQueryController {

    @Autowired
    private UserLoginService userLoginService;

    @Autowired
    private ScheduleQueryService scheduleQueryService;

    @Autowired
    private ScheduleCacheService scheduleCacheService;

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
     * @param refresh
     * @param bindingResult
     * @return
     */
    @RequestMapping(value = "/rest/schedulequery", method = RequestMethod.POST)
    @RestQueryLog
    @ResponseBody
    public ScheduleQueryJsonResult ScheduleQuery(HttpServletRequest request
            , @Validated(value = ServiceQueryValidGroup.class) User user
            , @RequestParam(value = "week", required = false) Integer week
            , @RequestParam(name = "refresh", required = false
            , defaultValue = "false") Boolean refresh, BindingResult bindingResult) {
        ScheduleQueryJsonResult scheduleQueryJsonResult = new ScheduleQueryJsonResult();
        if (bindingResult.hasErrors() || (week != null && (week < 0 || week > 20))) {
            scheduleQueryJsonResult.setSuccess(false);
            scheduleQueryJsonResult.setErrorMessage("请求参数不合法");
        } else {
            String username = user.getUsername();
            String keycode = user.getKeycode();
            String number = user.getNumber();
            //校验用户账号身份
            BaseResult<User, LoginResultEnum> loginResult = userLoginService.UserLogin(request, user, true);
            switch (loginResult.getResultType()) {
                case LOGIN_SUCCESS:
                    if (!refresh) {
                        //优先查询缓存数据
                        ScheduleDocument scheduleDocument = scheduleCacheService.ReadSchedule(username);
                        if (scheduleDocument != null) {
                            if (week == null) {
                                week = scheduleQueryService.getCurrentWeek();
                            }
                            scheduleQueryJsonResult.setSuccess(true);
                            scheduleQueryJsonResult.setScheduleList(scheduleQueryService
                                    .getSpecifiedWeekSchedule(scheduleDocument.getScheduleList(), week));
                            scheduleQueryJsonResult.setSelectedWeek(week);
                            return scheduleQueryJsonResult;
                        }
                    }
                    //若缓存数据不存在或要求强制更新，则从教务系统获取
                    BaseResult<List<Schedule>, ServiceResultEnum> scheduleQueryResult = scheduleQueryService
                            .ScheduleQuery(request, username, keycode, number);
                    switch (scheduleQueryResult.getResultType()) {
                        case SERVER_ERROR:
                            //服务器异常
                            scheduleQueryJsonResult.setSuccess(false);
                            scheduleQueryJsonResult.setErrorMessage("教务系统维护中，请稍候再试");
                            break;

                        case TIME_OUT:
                            //连接超时
                            scheduleQueryJsonResult.setSuccess(false);
                            scheduleQueryJsonResult.setErrorMessage("网络连接超时，请稍候再试");
                            break;

                        case PASSWORD_INCORRECT:
                            //用户名或密码错误
                            scheduleQueryJsonResult.setSuccess(false);
                            scheduleQueryJsonResult.setErrorMessage("密码已更新，请重新登录");
                            break;

                        case SUCCESS:
                            //查询成功
                            scheduleQueryJsonResult.setSuccess(true);
                            if (week == null) {
                                //无指定查询周数，则默认返回当前周数课表
                                scheduleQueryJsonResult.setScheduleList(scheduleQueryService
                                        .getSpecifiedWeekSchedule(scheduleQueryResult.getResultData()
                                                , scheduleQueryService.getCurrentWeek()));
                                scheduleQueryJsonResult.setSelectedWeek(scheduleQueryService.getCurrentWeek());
                            } else if (week.equals(0)) {
                                //若周数指定为0，则返回所有周数的课表
                                scheduleQueryJsonResult.setScheduleList(scheduleQueryResult.getResultData());
                                scheduleQueryJsonResult.setSelectedWeek(0);
                            } else {
                                //返回指定周数的课表
                                scheduleQueryJsonResult.setScheduleList(scheduleQueryService
                                        .getSpecifiedWeekSchedule(scheduleQueryResult.getResultData()
                                                , week));
                                scheduleQueryJsonResult.setSelectedWeek(week);
                            }
                            break;
                    }
                    break;

                case PASSWORD_ERROR:
                    //用户名或密码错误
                    scheduleQueryJsonResult.setSuccess(false);
                    scheduleQueryJsonResult.setErrorMessage("密码已更新，请重新登录");
                    break;

                case TIME_OUT:
                    //连接超时
                    scheduleQueryJsonResult.setSuccess(false);
                    scheduleQueryJsonResult.setErrorMessage("网络连接超时，请稍候再试");
                    break;

                case SERVER_ERROR:
                    //服务器异常
                    scheduleQueryJsonResult.setSuccess(false);
                    scheduleQueryJsonResult.setErrorMessage("教务系统维护中，请稍候再试");
                    break;
            }
        }
        return scheduleQueryJsonResult;
    }

    /**
     * 课表查询
     *
     * @param request
     * @param week
     * @return
     */
    @RequestMapping(value = "/schedulequery", method = RequestMethod.POST)
    @QueryLog
    @ResponseBody
    public ScheduleQueryJsonResult ScheduleQuery(HttpServletRequest request
            , @RequestParam(value = "week", required = false) Integer week
            , @RequestParam(value = "refresh", required = false, defaultValue = "false") Boolean refresh) {
        ScheduleQueryJsonResult scheduleQueryJsonResult = new ScheduleQueryJsonResult();
        if ((week != null && (week < 0 || week > 20))) {
            scheduleQueryJsonResult.setSuccess(false);
            scheduleQueryJsonResult.setErrorMessage("查询的周数不合法");
            return scheduleQueryJsonResult;
        }
        String username = (String) request.getSession().getAttribute("username");
        String keycode = (String) request.getSession().getAttribute("keycode");
        String number = (String) request.getSession().getAttribute("number");
        if (!refresh) {
            //优先查询缓存课表数据
            ScheduleDocument scheduleDocument = scheduleCacheService.ReadSchedule(username);
            if (scheduleDocument != null) {
                if (week == null) {
                    week = scheduleQueryService.getCurrentWeek();
                }
                List<Schedule> scheduleList = scheduleQueryService.getSpecifiedWeekSchedule(scheduleDocument
                        .getScheduleList(), week);
                scheduleQueryJsonResult.setSuccess(true);
                scheduleQueryJsonResult.setScheduleList(scheduleList);
                scheduleQueryJsonResult.setSelectedWeek(week);
                return scheduleQueryJsonResult;
            }
        }
        //若缓存数据不存在或要求强制更新，则从教务系统获取
        BaseResult<List<Schedule>, ServiceResultEnum> scheduleQueryResult = scheduleQueryService
                .ScheduleQuery(request, username, keycode, number);
        switch (scheduleQueryResult.getResultType()) {
            case SUCCESS:
                //查询课表成功
                scheduleQueryJsonResult.setSuccess(true);
                if (week == null) {
                    //无指定查询周数，则默认返回当前周数课表
                    scheduleQueryJsonResult.setScheduleList(scheduleQueryService
                            .getSpecifiedWeekSchedule(scheduleQueryResult.getResultData()
                                    , scheduleQueryService.getCurrentWeek()));
                    scheduleQueryJsonResult.setSelectedWeek(scheduleQueryService.getCurrentWeek());
                } else if (week.equals(0)) {
                    //若周数指定为0，则返回所有周数的课表
                    scheduleQueryJsonResult.setScheduleList(scheduleQueryResult.getResultData());
                    scheduleQueryJsonResult.setSelectedWeek(0);
                } else {
                    //返回指定周数的课表
                    scheduleQueryJsonResult.setScheduleList(scheduleQueryService
                            .getSpecifiedWeekSchedule(scheduleQueryResult.getResultData()
                                    , week));
                    scheduleQueryJsonResult.setSelectedWeek(week);
                }
                break;

            case TIME_OUT:
                //连接超时
                scheduleQueryJsonResult.setSuccess(false);
                scheduleQueryJsonResult.setErrorMessage("连接教务系统超时,请稍候再试");
                break;

            case SERVER_ERROR:
                //服务器异常
                scheduleQueryJsonResult.setSuccess(false);
                scheduleQueryJsonResult.setErrorMessage("教务系统维护中,请稍候再试");
                break;
        }
        return scheduleQueryJsonResult;
    }
}
