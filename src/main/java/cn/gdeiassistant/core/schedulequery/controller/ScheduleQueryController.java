package cn.gdeiassistant.core.schedulequery.controller;

import cn.gdeiassistant.common.annotation.QueryLogPersistence;
import cn.gdeiassistant.common.exception.CustomScheduleException.CountOverLimitException;
import cn.gdeiassistant.common.exception.DatabaseException.DataNotExistException;
import cn.gdeiassistant.common.exception.CustomScheduleException.GenerateScheduleException;
import cn.gdeiassistant.common.exception.QueryException.NotAvailableConditionException;
import cn.gdeiassistant.common.pojo.Entity.CustomSchedule;
import cn.gdeiassistant.common.pojo.Result.DataJsonResult;
import cn.gdeiassistant.common.pojo.Result.JsonResult;
import cn.gdeiassistant.core.schedulequery.pojo.ScheduleQueryResult;
import cn.gdeiassistant.core.schedulequery.service.ScheduleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;

@RestController
public class ScheduleQueryController {

    @Autowired
    private ScheduleService scheduleService;

    @RequestMapping(value = "/api/schedule", method = RequestMethod.GET)
    @QueryLogPersistence
    public DataJsonResult<ScheduleQueryResult> getSchedule(HttpServletRequest request
            , @RequestParam(value = "week", required = false) Integer week) throws Exception {
        String sessionId = (String) request.getAttribute("sessionId");
        ScheduleQueryResult scheduleQueryResult = scheduleService.querySchedule(sessionId, week);
        return new DataJsonResult<>(true, scheduleQueryResult);
    }

    /**
     * 强制刷新当前用户的课表缓存（清空 MongoDB 缓存并实时同步一次教务数据）
     * POST /api/schedule/update
     * 清空缓存并实时同步一次教务数据。
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/api/schedule/update", method = RequestMethod.POST)
    public JsonResult updateScheduleCache(HttpServletRequest request) throws Exception {
        String sessionId = (String) request.getAttribute("sessionId");
        scheduleService.updateScheduleCache(sessionId);
        return new JsonResult(true, "课表数据更新成功");
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
}
