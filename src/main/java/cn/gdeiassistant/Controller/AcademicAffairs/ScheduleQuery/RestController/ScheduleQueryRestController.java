package cn.gdeiassistant.Controller.AcademicAffairs.ScheduleQuery.RestController;

import cn.gdeiassistant.Annotation.QueryLogPersistence;
import cn.gdeiassistant.Annotation.RestAuthentication;
import cn.gdeiassistant.Annotation.RestQueryLogPersistence;
import cn.gdeiassistant.Annotation.TrialData;
import cn.gdeiassistant.Exception.CommonException.NetWorkTimeoutException;
import cn.gdeiassistant.Exception.CommonException.PasswordIncorrectException;
import cn.gdeiassistant.Exception.CommonException.ServerErrorException;
import cn.gdeiassistant.Exception.CustomScheduleException.CountOverLimitException;
import cn.gdeiassistant.Exception.CustomScheduleException.GenerateScheduleException;
import cn.gdeiassistant.Exception.QueryException.TimeStampIncorrectException;
import cn.gdeiassistant.Pojo.Entity.CustomSchedule;
import cn.gdeiassistant.Pojo.Result.DataJsonResult;
import cn.gdeiassistant.Pojo.Result.JsonResult;
import cn.gdeiassistant.Pojo.ScheduleQuery.ScheduleQueryResult;
import cn.gdeiassistant.Service.AcademicAffairs.ScheduleQuery.ScheduleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

@RestController
public class ScheduleQueryRestController {

    @Autowired
    private ScheduleService scheduleService;

    /**
     * 课表查询
     *
     * @param request
     * @param week
     * @return
     */
    @RequestMapping(value = "/api/schedulequery", method = RequestMethod.POST)
    @QueryLogPersistence
    @TrialData(value = "schedule", rest = false, responseTime = "week")
    public DataJsonResult<ScheduleQueryResult> ScheduleQuery(HttpServletRequest request
            , @Valid @Min(0) @Max(20) Integer week) throws Exception {
        ScheduleQueryResult scheduleQueryResult = scheduleService
                .QuerySchedule(request.getSession().getId(), week);
        return new DataJsonResult<>(true, scheduleQueryResult);
    }

    /**
     * 更新实时课表信息
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/api/refreshschedule", method = RequestMethod.POST)
    public DataJsonResult<ScheduleQueryResult> RefreshGradeData(HttpServletRequest request
            , @Valid @Min(0) @Max(20) Integer week) throws TimeStampIncorrectException, NetWorkTimeoutException, PasswordIncorrectException, ServerErrorException {
        scheduleService.ClearSchedule(request.getSession().getId());
        ScheduleQueryResult scheduleQueryResult = scheduleService.QuerySchedule(request.getSession()
                .getId(), week);
        return new DataJsonResult<>(true, scheduleQueryResult);
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
    public JsonResult AddCustomSchedule(HttpServletRequest request, @Validated CustomSchedule customSchedule)
            throws GenerateScheduleException, CountOverLimitException {
        scheduleService.AddCustomSchedule(request.getSession().getId(), customSchedule);
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
        scheduleService.DeleteCustomSchedule(request.getSession().getId(), id);
        return new JsonResult(true);
    }


    /**
     * 课表查询Rest接口
     *
     * @param request
     * @param token
     * @param week
     * @return
     */
    @RequestMapping(value = "/rest/schedulequery", method = RequestMethod.POST)
    @RestAuthentication
    @RestQueryLogPersistence
    @TrialData(value = "schedule", rest = true, responseTime = "week")
    public DataJsonResult<ScheduleQueryResult> ScheduleQuery(HttpServletRequest request
            , @RequestParam("token") String token
            , @Valid @Min(0) @Max(20) Integer week) throws Exception {
        String sessionId = (String) request.getAttribute("sessionId");
        ScheduleQueryResult scheduleQueryResult = scheduleService.QuerySchedule(sessionId, week);
        return new DataJsonResult<>(true, scheduleQueryResult);
    }
}
