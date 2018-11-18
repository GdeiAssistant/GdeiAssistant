package com.linguancheng.gdeiassistant.Controller.GradeQuery;

import com.linguancheng.gdeiassistant.Annotation.QueryLog;
import com.linguancheng.gdeiassistant.Annotation.RestAuthentication;
import com.linguancheng.gdeiassistant.Annotation.RestQueryLog;
import com.linguancheng.gdeiassistant.Enum.Base.QueryMethodEnum;
import com.linguancheng.gdeiassistant.Pojo.Entity.User;
import com.linguancheng.gdeiassistant.Pojo.GradeQuery.GradeQueryJsonResult;
import com.linguancheng.gdeiassistant.Pojo.GradeQuery.GradeQueryResult;
import com.linguancheng.gdeiassistant.Service.GradeQuery.GradeQueryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;

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
    @QueryLog
    @ResponseBody
    public GradeQueryJsonResult GradeQuery(HttpServletRequest request
            , Integer year, @RequestParam(value = "method", required = false
            , defaultValue = "0") QueryMethodEnum method) throws Exception {
        if ((year != null && (year < 0 || year > 3)) || method == null) {
            GradeQueryJsonResult result = new GradeQueryJsonResult();
            result.setSuccess(false);
            result.setMessage("请求参数不合法");
            return result;
        } else {
            String username = (String) request.getSession().getAttribute("username");
            String password = (String) request.getSession().getAttribute("password");
            User user = new User(username, password);
            return HandleGradeQuery(request.getSession().getId(), user, year, method);
        }
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
    @RestQueryLog
    @RestAuthentication
    @ResponseBody
    public GradeQueryJsonResult GradeQuery(HttpServletRequest request
            , @RequestParam("token") String token, Integer year, @RequestParam(value = "method", required = false
            , defaultValue = "0") QueryMethodEnum method) throws Exception {
        if ((year != null && (year < 0 || year > 3)) || method == null) {
            GradeQueryJsonResult result = new GradeQueryJsonResult();
            result.setSuccess(false);
            result.setMessage("请求参数不合法");
            return result;
        } else {
            User user = (User) request.getAttribute("user");
            return HandleGradeQuery(request.getSession().getId(), user, year, method);
        }
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
    private GradeQueryJsonResult HandleGradeQuery(String sessionId, User user, Integer year
            , QueryMethodEnum method) throws Exception {
        GradeQueryJsonResult result = new GradeQueryJsonResult();
        GradeQueryResult gradeQueryResult = null;
        switch (method) {
            case CACHE_FIRST:
                //优先查询缓存
                gradeQueryResult = gradeQueryService.QueryGrade(sessionId, user, year);
                result.setSuccess(true);
                result.setFirstTermGPA(gradeQueryResult.getFirstTermGPA());
                result.setFirstTermIGP(gradeQueryResult.getFirstTermIGP());
                result.setSecondTermGPA(gradeQueryResult.getSecondTermGPA());
                result.setSecondTermIGP(gradeQueryResult.getSecondTermIGP());
                result.setFirstTermGradeList(gradeQueryResult.getFirstTermGradeList());
                result.setSecondTermGradeList(gradeQueryResult.getSecondTermGradeList());
                result.setYear(gradeQueryResult.getYear());
                break;

            case CACHE_ONLY:
                //只查询缓存
                gradeQueryResult = gradeQueryService.QueryUserGradeFromDocument(user.getUsername(), year);
                result.setSuccess(true);
                result.setFirstTermGPA(gradeQueryResult == null ? 0 : gradeQueryResult.getFirstTermGPA());
                result.setFirstTermIGP(gradeQueryResult == null ? 0 : gradeQueryResult.getFirstTermIGP());
                result.setSecondTermGPA(gradeQueryResult == null ? 0 : gradeQueryResult.getSecondTermGPA());
                result.setSecondTermIGP(gradeQueryResult == null ? 0 : gradeQueryResult.getSecondTermIGP());
                result.setFirstTermGradeList(gradeQueryResult == null ? new ArrayList<>() : gradeQueryResult.getFirstTermGradeList());
                result.setSecondTermGradeList(gradeQueryResult == null ? new ArrayList<>() : gradeQueryResult.getSecondTermGradeList());
                result.setYear(gradeQueryResult == null ? 0 : gradeQueryResult.getYear());
                break;

            case QUERY_ONLY:
                //只查询教务系统
                gradeQueryResult = gradeQueryService.QueryGradeFromSystem(sessionId, user, year);
                //查询成功
                result.setSuccess(true);
                result.setFirstTermGPA(gradeQueryResult.getFirstTermGPA());
                result.setFirstTermIGP(gradeQueryResult.getFirstTermIGP());
                result.setSecondTermGPA(gradeQueryResult.getSecondTermGPA());
                result.setSecondTermIGP(gradeQueryResult.getSecondTermIGP());
                result.setFirstTermGradeList(gradeQueryResult.getFirstTermGradeList());
                result.setSecondTermGradeList(gradeQueryResult.getSecondTermGradeList());
                result.setYear(gradeQueryResult.getYear());
                break;
        }
        return result;
    }
}
