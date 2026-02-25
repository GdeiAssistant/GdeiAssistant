package cn.gdeiassistant.core.gradequery.controller;

import cn.gdeiassistant.common.annotation.QueryLogPersistence;
import cn.gdeiassistant.common.annotation.RestQueryLogPersistence;
import cn.gdeiassistant.common.annotation.TrialData;
import cn.gdeiassistant.core.gradequery.pojo.GradeQueryResult;
import cn.gdeiassistant.common.pojo.Result.JsonResult;
import cn.gdeiassistant.common.pojo.Result.DataJsonResult;
import cn.gdeiassistant.core.gradequery.service.GradeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

@RestController
public class GradeQueryController {

    @Autowired
    private GradeService gradeService;

    /**
     * 成绩查询（RESTful GET，供前端直接调用）
     * 从 MongoDB 缓存或教务系统获取成绩，测试账号 gdeiassistant 走 MongoDB grade 集合。
     * 异常交由 GlobalRestExceptionHandler 脱敏返回。
     *
     * @param request 当前会话，用于解析登录用户
     * @param year    可选，学年索引 0~3（大一~大四），不传则默认最近学年
     * @return DataJsonResult&lt;GradeQueryResult&gt;，data 含 firstTermGradeList、secondTermGradeList 等
     */
    @RequestMapping(value = "/api/grade", method = RequestMethod.GET)
    @TrialData(value = "grade")
    public DataJsonResult<GradeQueryResult> getGrade(HttpServletRequest request,
                                                      @RequestParam(required = false) Integer year) throws Exception {
        String sessionId = (String) request.getAttribute("sessionId");
        GradeQueryResult gradeQueryResult = gradeService.queryGrade(sessionId, year);
        return new DataJsonResult<>(true, gradeQueryResult);
    }

    /**
     * 强制刷新当前用户的成绩缓存（清空 MongoDB 缓存并实时同步一次教务数据）
     * POST /api/grade/update
     * 测试账号由 TrialDataAspect 拦截，禁止触发真实教务刷新。
     *
     * @param request
     * @return
     * @throws Exception 由全局异常处理器进行脱敏处理（含测试账号受限提示）
     */
    @RequestMapping(value = "/api/grade/update", method = RequestMethod.POST)
    @TrialData(value = "graderefresh")
    public JsonResult updateGradeCache(HttpServletRequest request) throws Exception {
        String sessionId = (String) request.getAttribute("sessionId");
        gradeService.updateGradeCache(sessionId);
        return new JsonResult(true, "成绩数据更新成功");
    }

    /**
     * 成绩查询（POST，保留兼容）
     *
     * @param request
     * @param year
     * @return
     */
    @RequestMapping(value = "/api/gradeQuery", method = RequestMethod.POST)
    @QueryLogPersistence
    public DataJsonResult<GradeQueryResult> gradeQuery(HttpServletRequest request
            , @Valid @Min(0) @Max(3) Integer year) throws Exception {
        String sessionId = (String) request.getAttribute("sessionId");
        GradeQueryResult gradeQueryResult = gradeService.queryGrade(sessionId, year);
        return new DataJsonResult<>(true, gradeQueryResult);
    }

    /**
     * 成绩查询Rest接口
     *
     * @param request
     * @param token
     * @param year
     * @return
     */
    @RequestMapping(value = "/api/grade/query", method = RequestMethod.POST)
    @RestQueryLogPersistence
    public DataJsonResult<GradeQueryResult> gradeQuery(HttpServletRequest request
            , @RequestParam("token") String token
            , @Valid @Min(0) @Max(3) Integer year) throws Exception {
        String sessionId = (String) request.getAttribute("sessionId");
        GradeQueryResult gradeQueryResult = gradeService.queryGrade(sessionId, year);
        return new DataJsonResult<>(true, gradeQueryResult);
    }

    /**
     * 刷新实时成绩信息（清空缓存后重新查询，可能触发真实教务请求）
     * 测试账号由 TrialDataAspect 拦截，禁止穿透。
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/api/refreshgrade", method = RequestMethod.POST)
    @QueryLogPersistence
    @TrialData(value = "graderefresh")
    public DataJsonResult<GradeQueryResult> refreshGradeData(HttpServletRequest request
            , @Valid @Min(0) @Max(3) Integer year) throws Exception {
        String sessionId = (String) request.getAttribute("sessionId");
        //清空缓存的成绩信息
        gradeService.clearGrade(sessionId);
        //重新查询成绩信息
        GradeQueryResult gradeQueryResult = gradeService.queryGrade(sessionId, year);
        return new DataJsonResult<>(true, gradeQueryResult);
    }
}
