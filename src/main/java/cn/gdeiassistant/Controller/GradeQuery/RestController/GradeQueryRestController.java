package cn.gdeiassistant.Controller.GradeQuery.RestController;

import cn.gdeiassistant.Annotation.*;
import cn.gdeiassistant.Annotation.*;
import cn.gdeiassistant.Enum.Method.QueryMethodEnum;
import cn.gdeiassistant.Pojo.Entity.User;
import cn.gdeiassistant.Pojo.GradeQuery.GradeQueryResult;
import cn.gdeiassistant.Pojo.Result.DataJsonResult;
import cn.gdeiassistant.Pojo.Result.JsonResult;
import cn.gdeiassistant.Service.GradeQuery.GradeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
public class GradeQueryRestController {

    @Autowired
    private GradeService gradeService;

    /**
     * 清空缓存成绩信息
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/api/refreshgrade", method = RequestMethod.POST)
    @UserGroupAccess(group = {2, 3}, rest = true)
    public JsonResult RefreshGradeData(HttpServletRequest request) {
        String username = (String) request.getSession().getAttribute("username");
        gradeService.ClearGrade(username);
        return new JsonResult(true);
    }

    /**
     * 成绩查询
     *
     * @param request
     * @param year
     * @param method
     * @return
     */
    @RequestMapping(value = "/api/gradequery", method = RequestMethod.POST)
    @QueryLogPersistence
    @TrialData(value = "grade", requestTime = "year", responseTime = "year")
    @UserGroupAccess(group = {2, 3, 7}, rest = true)
    public DataJsonResult<GradeQueryResult> GradeQuery(HttpServletRequest request
            , Integer year, @RequestParam(value = "method", required = false
            , defaultValue = "0") QueryMethodEnum method) throws Exception {
        if ((year != null && (year < 0 || year > 3)) || method == null) {
            return new DataJsonResult<>(new JsonResult(false, "请求参数不合法"));
        }
        String username = (String) request.getSession().getAttribute("username");
        String password = (String) request.getSession().getAttribute("password");
        User user = new User(username, password);
        GradeQueryResult gradeQueryResult = HandleGradeQuery(request.getSession().getId(), user, year, method);
        return new DataJsonResult<>(true, gradeQueryResult);
    }

    /**
     * 成绩查询Rest接口
     *
     * @param request
     * @param token
     * @param year
     * @param method
     * @return
     */
    @RequestMapping(value = "/rest/gradequery", method = RequestMethod.POST)
    @RestQueryLogPersistence
    @RestAuthentication
    @TrialData(value = "grade", requestTime = "year", responseTime = "year")
    @UserGroupAccess(group = {2, 3, 7}, rest = true)
    public DataJsonResult<GradeQueryResult> GradeQuery(HttpServletRequest request
            , @RequestParam("token") String token, Integer year, @RequestParam(value = "method", required = false
            , defaultValue = "0") QueryMethodEnum method) throws Exception {
        if ((year != null && (year < 0 || year > 3)) || method == null) {
            return new DataJsonResult<>(new JsonResult(false, "请求参数不合法"));
        }
        User user = (User) request.getAttribute("user");
        GradeQueryResult gradeQueryResult = HandleGradeQuery(request.getSession().getId(), user, year, method);
        return new DataJsonResult<>(true, gradeQueryResult);
    }

    /**
     * 处理成绩查询请求
     *
     * @param sessionId
     * @param user
     * @param year
     * @param method
     * @return
     */
    private GradeQueryResult HandleGradeQuery(String sessionId, User user, Integer year
            , QueryMethodEnum method) throws Exception {
        switch (method) {
            case CACHE_ONLY:
                //只查询缓存
                return gradeService.QueryUserGradeFromDocument(user.getUsername(), year);

            case QUERY_ONLY:
                //只查询教务系统
                return gradeService.QueryGradeFromSystem(sessionId, user, year);

            case CACHE_FIRST:
            default:
                //优先查询缓存
                return gradeService.QueryGrade(sessionId, user, year);
        }
    }
}
