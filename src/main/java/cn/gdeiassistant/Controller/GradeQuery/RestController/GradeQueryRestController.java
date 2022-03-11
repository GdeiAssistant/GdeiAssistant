package cn.gdeiassistant.Controller.GradeQuery.RestController;

import cn.gdeiassistant.Annotation.QueryLogPersistence;
import cn.gdeiassistant.Annotation.RestAuthentication;
import cn.gdeiassistant.Annotation.RestQueryLogPersistence;
import cn.gdeiassistant.Annotation.TrialData;
import cn.gdeiassistant.Pojo.GradeQuery.GradeQueryResult;
import cn.gdeiassistant.Pojo.Result.DataJsonResult;
import cn.gdeiassistant.Service.GradeQuery.GradeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

@RestController
public class GradeQueryRestController {

    @Autowired
    private GradeService gradeService;

    /**
     * 成绩查询
     *
     * @param request
     * @param year
     * @return
     */
    @RequestMapping(value = "/api/gradequery", method = RequestMethod.POST)
    @QueryLogPersistence
    @TrialData(value = "grade", rest = false, requestTime = "year", responseTime = "year")
    public DataJsonResult<GradeQueryResult> GradeQuery(HttpServletRequest request
            , @Valid @Min(0) @Max(3) Integer year) throws Exception {
        GradeQueryResult gradeQueryResult = gradeService.QueryGrade(request.getSession().getId(), year);
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
    @RequestMapping(value = "/rest/gradequery", method = RequestMethod.POST)
    @RestQueryLogPersistence
    @RestAuthentication
    @TrialData(value = "grade", rest = true, requestTime = "year", responseTime = "year")
    public DataJsonResult<GradeQueryResult> GradeQuery(HttpServletRequest request
            , @RequestParam("token") String token
            , @Valid @Min(0) @Max(3) Integer year) throws Exception {
        String sessionId = (String) request.getAttribute("sessionId");
        GradeQueryResult gradeQueryResult = gradeService.QueryGrade(sessionId, year);
        return new DataJsonResult<>(true, gradeQueryResult);
    }

    /**
     * 刷新实时成绩信息
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/api/refreshgrade", method = RequestMethod.POST)
    @QueryLogPersistence
    @TrialData(value = "grade", rest = false, requestTime = "year", responseTime = "year")
    public DataJsonResult<GradeQueryResult> RefreshGradeData(HttpServletRequest request
            , @Valid @Min(0) @Max(3) Integer year) throws Exception {
        //清空缓存的成绩信息
        gradeService.ClearGrade(request.getSession().getId());
        //重新查询成绩信息
        GradeQueryResult gradeQueryResult = gradeService.QueryGrade(request.getSession().getId(), year);
        return new DataJsonResult<>(true, gradeQueryResult);
    }
}
