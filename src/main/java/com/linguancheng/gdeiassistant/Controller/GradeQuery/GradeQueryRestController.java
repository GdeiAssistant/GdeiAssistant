package com.linguancheng.gdeiassistant.Controller.GradeQuery;

import com.linguancheng.gdeiassistant.Annotation.QueryLogPersistence;
import com.linguancheng.gdeiassistant.Annotation.RestAuthentication;
import com.linguancheng.gdeiassistant.Annotation.RestQueryLogPersistence;
import com.linguancheng.gdeiassistant.Enum.Base.QueryMethodEnum;
import com.linguancheng.gdeiassistant.Pojo.Entity.User;
import com.linguancheng.gdeiassistant.Pojo.GradeQuery.GradeQueryResult;
import com.linguancheng.gdeiassistant.Pojo.Result.DataJsonResult;
import com.linguancheng.gdeiassistant.Pojo.Result.JsonResult;
import com.linguancheng.gdeiassistant.Service.GradeQuery.GradeQueryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
public class GradeQueryRestController {

    @Autowired
    private GradeQueryService gradeQueryService;

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
        GradeQueryResult gradeQueryResult = null;
        switch (method) {
            case CACHE_ONLY:
                //只查询缓存
                return gradeQueryService.QueryUserGradeFromDocument(user.getUsername(), year);

            case QUERY_ONLY:
                //只查询教务系统
                return gradeQueryService.QueryGradeFromSystem(sessionId, user, year);

            case CACHE_FIRST:
            default:
                //优先查询缓存
                return gradeQueryService.QueryGrade(sessionId, user, year);
        }
    }
}
