package cn.gdeiassistant.core.schedulequery.controller;

import cn.gdeiassistant.common.annotation.QueryLogPersistence;
import cn.gdeiassistant.common.annotation.RestQueryLogPersistence;
import cn.gdeiassistant.common.annotation.TrialData;
import cn.gdeiassistant.common.exception.CommonException.NetWorkTimeoutException;
import cn.gdeiassistant.common.exception.CommonException.PasswordIncorrectException;
import cn.gdeiassistant.common.exception.CommonException.ServerErrorException;
import cn.gdeiassistant.common.exception.CustomScheduleException.CountOverLimitException;
import cn.gdeiassistant.common.exception.DatabaseException.DataNotExistException;
import cn.gdeiassistant.common.exception.CustomScheduleException.GenerateScheduleException;
import cn.gdeiassistant.common.exception.CommonException.TestAccountException;
import cn.gdeiassistant.common.exception.QueryException.NotAvailableConditionException;
import cn.gdeiassistant.common.exception.QueryException.TimeStampIncorrectException;
import cn.gdeiassistant.common.pojo.Entity.CustomSchedule;
import cn.gdeiassistant.common.pojo.Result.DataJsonResult;
import cn.gdeiassistant.common.pojo.Result.JsonResult;
import cn.gdeiassistant.core.schedulequery.pojo.ScheduleQueryResult;
import cn.gdeiassistant.core.schedulequery.service.ScheduleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

@RestController
public class ScheduleQueryController {

    @Autowired
    private ScheduleService scheduleService;

    @RequestMapping(value = "/api/schedule", method = RequestMethod.GET)
    @QueryLogPersistence
    @TrialData(value = "schedule")
    public DataJsonResult<ScheduleQueryResult> getSchedule(HttpServletRequest request
            , @RequestParam(value = "week", required = false) Integer week) throws Exception {
        String sessionId = (String) request.getAttribute("sessionId");
        ScheduleQueryResult scheduleQueryResult = scheduleService.querySchedule(sessionId, week);
        return new DataJsonResult<>(true, scheduleQueryResult);
    }

    /**
     * 强制刷新当前用户的课表缓存（清空 MongoDB 缓存并实时同步一次教务数据）
     * POST /api/schedule/update
     * 测试账号会抛出 TestAccountException，此处捕获后返回成功提示，不阻断前端后续获取课表（含自定义课表）展示。
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/api/schedule/update", method = RequestMethod.POST)
    public JsonResult updateScheduleCache(HttpServletRequest request) throws Exception {
        String sessionId = (String) request.getAttribute("sessionId");
        try {
            scheduleService.updateScheduleCache(sessionId);
            return new JsonResult(true, "课表数据更新成功");
        } catch (TestAccountException e) {
            return new JsonResult(true, "测试账号不更新教务数据，请直接查看课表");
        }
    }

    /**
     * 添加自定义课程（REST JSON）
     * POST /api/schedule/custom
     */
    @RequestMapping(value = "/api/schedule/custom", method = RequestMethod.POST)
    public JsonResult addCustomSchedule(HttpServletRequest request, @RequestBody @Validated CustomSchedule customSchedule)
            throws GenerateScheduleException, CountOverLimitException, NotAvailableConditionException {
        String sessionId = (String) request.getAttribute("sessionId");
        scheduleService.addCustomSchedule(sessionId, customSchedule);
        return new JsonResult(true, "添加成功");
    }

    /**
     * 删除自定义课程（按 position 定位，CustomSchedule 无 id 字段）
     * DELETE /api/schedule/custom?position=xxx
     */
    @RequestMapping(value = "/api/schedule/custom", method = RequestMethod.DELETE)
    public JsonResult deleteCustomSchedule(HttpServletRequest request, @RequestParam("position") Integer position) throws DataNotExistException {
        String sessionId = (String) request.getAttribute("sessionId");
        scheduleService.deleteCustomSchedule(sessionId, position);
        return new JsonResult(true);
    }

    @RequestMapping(value = "/api/schedulequery", method = RequestMethod.POST)
    @QueryLogPersistence
    public DataJsonResult<ScheduleQueryResult> scheduleQuery(HttpServletRequest request
            , @Valid @Min(0) @Max(20) Integer week) throws Exception {
        String sessionId = (String) request.getAttribute("sessionId");
        ScheduleQueryResult scheduleQueryResult = scheduleService.querySchedule(sessionId, week);
        return new DataJsonResult<>(true, scheduleQueryResult);
    }

    /**
     * 更新实时课表信息
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/api/refreshschedule", method = RequestMethod.POST)
    public DataJsonResult<ScheduleQueryResult> refreshScheduleData(HttpServletRequest request
            , @Valid @Min(0) @Max(20) Integer week) throws TimeStampIncorrectException, NetWorkTimeoutException, PasswordIncorrectException, ServerErrorException {
        String sessionId = (String) request.getAttribute("sessionId");
        scheduleService.clearSchedule(sessionId);
        ScheduleQueryResult scheduleQueryResult = scheduleService.querySchedule(sessionId, week);
        return new DataJsonResult<>(true, scheduleQueryResult);
    }

    /**
     * 课表查询Rest接口
     *
     * @param request
     * @param token
     * @param week
     * @return
     */
    @RequestMapping(value = "/api/schedule/query", method = RequestMethod.POST)
    @RestQueryLogPersistence
    public DataJsonResult<ScheduleQueryResult> scheduleQueryRest(HttpServletRequest request
            , @RequestParam("token") String token
            , @Valid @Min(0) @Max(20) Integer week) throws Exception {
        String sessionId = (String) request.getAttribute("sessionId");
        ScheduleQueryResult scheduleQueryResult = scheduleService.querySchedule(sessionId, week);
        return new DataJsonResult<>(true, scheduleQueryResult);
    }
}
