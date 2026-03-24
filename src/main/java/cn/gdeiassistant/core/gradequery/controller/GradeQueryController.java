package cn.gdeiassistant.core.gradequery.controller;

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

@RestController
public class GradeQueryController {

    @Autowired
    private GradeService gradeService;

    /**
     * 成绩查询（RESTful GET，供前端直接调用）
     * 从 MongoDB 缓存或教务系统获取成绩。
     * 异常交由 GlobalRestExceptionHandler 脱敏返回。
     *
     * @param request 当前会话，用于解析登录用户
     * @param year    可选，学年索引 0~3（大一~大四），不传则默认最近学年
     * @return DataJsonResult&lt;GradeQueryResult&gt;，data 含 firstTermGradeList、secondTermGradeList 等
     */
    @RequestMapping(value = "/api/grade", method = RequestMethod.GET)
    public DataJsonResult<GradeQueryResult> getGrade(HttpServletRequest request,
                                                      @RequestParam(required = false) Integer year) throws Exception {
        String sessionId = (String) request.getAttribute("sessionId");
        GradeQueryResult gradeQueryResult = gradeService.queryGrade(sessionId, year);
        return new DataJsonResult<>(true, gradeQueryResult);
    }

    /**
     * 强制刷新当前用户的成绩缓存（清空 MongoDB 缓存并实时同步一次教务数据）
     * POST /api/grade/update
     * 清空缓存并实时同步一次教务数据。
     *
     * @param request
     * @return
     * @throws Exception 由全局异常处理器进行脱敏处理
     */
    @RequestMapping(value = "/api/grade/update", method = RequestMethod.POST)
    public JsonResult updateGradeCache(HttpServletRequest request) throws Exception {
        String sessionId = (String) request.getAttribute("sessionId");
        gradeService.updateGradeCache(sessionId);
        return new JsonResult(true, "成绩数据更新成功");
    }
}
