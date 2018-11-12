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
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by linguancheng on 2017/7/22.
 */

@Controller
public class GradeQueryController {

    @Autowired
    private GradeQueryService gradeQueryService;

    @RequestMapping(value = "/grade")
    public ModelAndView ResolveGradePage() {
        return new ModelAndView("Grade/grade");
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
            , defaultValue = "0") QueryMethodEnum method) {
        GradeQueryJsonResult result = new GradeQueryJsonResult();
        if ((year != null && (year < 0 || year > 3)) || method == null) {
            result.setSuccess(false);
            result.setMessage("请求参数不合法");
        } else {
            User user = (User) request.getAttribute("user");
            GradeQueryResult gradeQueryResult = null;
            switch (method) {
                case CACHE_FIRST:
                    //优先查询缓存
                    gradeQueryResult = gradeQueryService.GetUserGradeDocument(user
                            .getUsername(), year);
                    switch (gradeQueryResult.getGradeServiceResultEnum()) {
                        case SUCCESS:
                            //查询成功
                            result.setSuccess(true);
                            result.setFirstTermGPA(gradeQueryResult.getFirstTermGPA());
                            result.setFirstTermIGP(gradeQueryResult.getFirstTermIGP());
                            result.setSecondTermGPA(gradeQueryResult.getSecondTermGPA());
                            result.setSecondTermIGP(gradeQueryResult.getSecondTermIGP());
                            result.setFirstTermGradeList(gradeQueryResult.getFirstTermGradeList());
                            result.setSecondTermGradeList(gradeQueryResult.getSecondTermGradeList());
                            result.setQueryYear(gradeQueryResult.getQueryYear());
                            break;

                        case EMPTY_RESULT:
                            //缓存无数据，获取教务系统成绩数据
                            gradeQueryResult = gradeQueryService.QueryGradeData(request.getSession().getId()
                                    , user, year);
                            switch (gradeQueryResult.getGradeServiceResultEnum()) {
                                case SUCCESS:
                                    //查询成功
                                    result.setSuccess(true);
                                    result.setFirstTermGPA(gradeQueryResult.getFirstTermGPA());
                                    result.setFirstTermIGP(gradeQueryResult.getFirstTermIGP());
                                    result.setSecondTermGPA(gradeQueryResult.getSecondTermGPA());
                                    result.setSecondTermIGP(gradeQueryResult.getSecondTermIGP());
                                    result.setFirstTermGradeList(gradeQueryResult.getFirstTermGradeList());
                                    result.setSecondTermGradeList(gradeQueryResult.getSecondTermGradeList());
                                    result.setQueryYear(gradeQueryResult.getQueryYear());
                                    break;

                                case ERROR_CONDITION:
                                    //学期不可查询
                                    result.setSuccess(false);
                                    result.setEmpty(true);
                                    result.setMessage("当前学期暂不可查询");
                                    break;

                                case PASSWORD_INCORRECT:
                                    //用户密码错误
                                    result.setSuccess(false);
                                    result.setEmpty(false);
                                    result.setMessage("密码已更新，请重新登录");
                                    break;

                                case TIME_OUT:
                                    //网络连接超时
                                    result.setSuccess(false);
                                    result.setEmpty(false);
                                    result.setMessage("网络连接超时，请重试");
                                    break;

                                case TIMESTAMP_INVALID:
                                    //时间戳失效
                                    result.setSuccess(false);
                                    result.setEmpty(false);
                                    result.setMessage("时间戳校验失败，请尝试重新登录");
                                    break;

                                case SERVER_ERROR:
                                default:
                                    //教务系统异常
                                    result.setSuccess(false);
                                    result.setEmpty(false);
                                    result.setMessage("教务系统维护中，请稍后再试");
                                    break;
                            }
                            break;

                        case ERROR_CONDITION:
                            //学期不可查询
                            result.setSuccess(false);
                            result.setEmpty(true);
                            result.setMessage("当前学期暂不可查询");
                            break;
                    }
                    break;

                case CACHE_ONLY:
                    //只查询缓存
                    gradeQueryResult = gradeQueryService.GetUserGradeDocument(user
                            .getUsername(), year);
                    switch (gradeQueryResult.getGradeServiceResultEnum()) {
                        case SUCCESS:
                            //查询成功
                            result.setSuccess(true);
                            result.setFirstTermGPA(gradeQueryResult.getFirstTermGPA());
                            result.setFirstTermIGP(gradeQueryResult.getFirstTermIGP());
                            result.setSecondTermGPA(gradeQueryResult.getSecondTermGPA());
                            result.setSecondTermIGP(gradeQueryResult.getSecondTermIGP());
                            result.setFirstTermGradeList(gradeQueryResult.getFirstTermGradeList());
                            result.setSecondTermGradeList(gradeQueryResult.getSecondTermGradeList());
                            result.setQueryYear(gradeQueryResult.getQueryYear());
                            break;

                        case EMPTY_RESULT:
                            //缓存无数据
                            result.setSuccess(false);
                            result.setEmpty(false);
                            result.setMessage("用户成绩信息未同步，请稍后再试");
                            break;

                        case ERROR_CONDITION:
                            //学期不可查询
                            result.setSuccess(false);
                            result.setEmpty(true);
                            result.setMessage("当前学期暂不可查询");
                            break;
                    }
                    break;

                case QUERY_ONLY:
                    //只查询教务系统
                    gradeQueryResult = gradeQueryService.QueryGradeData(request.getSession().getId(), user
                            , year);
                    switch (gradeQueryResult.getGradeServiceResultEnum()) {
                        case SUCCESS:
                            //查询成功
                            result.setSuccess(true);
                            result.setFirstTermGPA(gradeQueryResult.getFirstTermGPA());
                            result.setFirstTermIGP(gradeQueryResult.getFirstTermIGP());
                            result.setSecondTermGPA(gradeQueryResult.getSecondTermGPA());
                            result.setSecondTermIGP(gradeQueryResult.getSecondTermIGP());
                            result.setFirstTermGradeList(gradeQueryResult.getFirstTermGradeList());
                            result.setSecondTermGradeList(gradeQueryResult.getSecondTermGradeList());
                            result.setQueryYear(gradeQueryResult.getQueryYear());
                            break;

                        case ERROR_CONDITION:
                            //学期不可查询
                            result.setSuccess(false);
                            result.setEmpty(true);
                            result.setMessage("当前学期暂不可查询");
                            break;

                        case PASSWORD_INCORRECT:
                            //用户密码错误
                            result.setSuccess(false);
                            result.setEmpty(false);
                            result.setMessage("密码已更新，请重新登录");
                            break;

                        case TIME_OUT:
                            //网络连接超时
                            result.setSuccess(false);
                            result.setEmpty(false);
                            result.setMessage("网络连接超时，请重试");
                            break;

                        case TIMESTAMP_INVALID:
                            //时间戳失效
                            result.setSuccess(false);
                            result.setEmpty(false);
                            result.setMessage("时间戳校验失败，请尝试重新登录");
                            break;

                        case SERVER_ERROR:
                        default:
                            //教务系统异常
                            result.setSuccess(false);
                            result.setEmpty(false);
                            result.setMessage("教务系统维护中，请稍后再试");
                            break;
                    }
                    break;
            }
        }
        return result;
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
    @QueryLog
    @ResponseBody
    public GradeQueryJsonResult GradeQuery(HttpServletRequest request
            , Integer year, @RequestParam(value = "method", required = false
            , defaultValue = "0") QueryMethodEnum method) {
        GradeQueryJsonResult result = new GradeQueryJsonResult();
        if ((year != null && (year < 0 || year > 3)) || method == null) {
            result.setSuccess(false);
            result.setMessage("请求参数不合法");
        } else {
            String username = (String) request.getSession().getAttribute("username");
            String password = (String) request.getSession().getAttribute("password");
            GradeQueryResult gradeQueryResult = null;
            switch (method) {
                case CACHE_FIRST:
                    //优先查询缓存
                    gradeQueryResult = gradeQueryService.GetUserGradeDocument(username, year);
                    switch (gradeQueryResult.getGradeServiceResultEnum()) {
                        case SUCCESS:
                            //查询成功
                            result.setSuccess(true);
                            result.setFirstTermGPA(gradeQueryResult.getFirstTermGPA());
                            result.setFirstTermIGP(gradeQueryResult.getFirstTermIGP());
                            result.setSecondTermGPA(gradeQueryResult.getSecondTermGPA());
                            result.setSecondTermIGP(gradeQueryResult.getSecondTermIGP());
                            result.setFirstTermGradeList(gradeQueryResult.getFirstTermGradeList());
                            result.setSecondTermGradeList(gradeQueryResult.getSecondTermGradeList());
                            result.setQueryYear(gradeQueryResult.getQueryYear());
                            break;

                        case EMPTY_RESULT:
                            //缓存无数据，获取教务系统成绩数据
                            gradeQueryResult = gradeQueryService.QueryGradeData(request.getSession().getId()
                                    , new User(username, password), year);
                            switch (gradeQueryResult.getGradeServiceResultEnum()) {
                                case SUCCESS:
                                    //查询成功
                                    result.setSuccess(true);
                                    result.setFirstTermGPA(gradeQueryResult.getFirstTermGPA());
                                    result.setFirstTermIGP(gradeQueryResult.getFirstTermIGP());
                                    result.setSecondTermGPA(gradeQueryResult.getSecondTermGPA());
                                    result.setSecondTermIGP(gradeQueryResult.getSecondTermIGP());
                                    result.setFirstTermGradeList(gradeQueryResult.getFirstTermGradeList());
                                    result.setSecondTermGradeList(gradeQueryResult.getSecondTermGradeList());
                                    result.setQueryYear(gradeQueryResult.getQueryYear());
                                    break;

                                case ERROR_CONDITION:
                                    //学期不可查询
                                    result.setSuccess(false);
                                    result.setEmpty(true);
                                    result.setMessage("当前学期暂不可查询");
                                    break;

                                case PASSWORD_INCORRECT:
                                    //用户密码错误
                                    result.setSuccess(false);
                                    result.setEmpty(false);
                                    result.setMessage("密码已更新，请重新登录");
                                    break;

                                case TIME_OUT:
                                    //网络连接超时
                                    result.setSuccess(false);
                                    result.setEmpty(false);
                                    result.setMessage("网络连接超时，请重试");
                                    break;

                                case TIMESTAMP_INVALID:
                                    //时间戳失效
                                    result.setSuccess(false);
                                    result.setEmpty(false);
                                    result.setMessage("时间戳校验失败，请尝试重新登录");
                                    break;

                                case SERVER_ERROR:
                                default:
                                    //教务系统异常
                                    result.setSuccess(false);
                                    result.setEmpty(false);
                                    result.setMessage("教务系统维护中，请稍后再试");
                                    break;
                            }
                            break;

                        case ERROR_CONDITION:
                            //学期不可查询
                            result.setSuccess(false);
                            result.setEmpty(true);
                            result.setMessage("当前学期暂不可查询");
                            break;
                    }
                    break;

                case CACHE_ONLY:
                    gradeQueryResult = gradeQueryService.GetUserGradeDocument(username, year);
                    switch (gradeQueryResult.getGradeServiceResultEnum()) {
                        case SUCCESS:
                            //查询成功
                            result.setSuccess(true);
                            result.setFirstTermGPA(gradeQueryResult.getFirstTermGPA());
                            result.setFirstTermIGP(gradeQueryResult.getFirstTermIGP());
                            result.setSecondTermGPA(gradeQueryResult.getSecondTermGPA());
                            result.setSecondTermIGP(gradeQueryResult.getSecondTermIGP());
                            result.setFirstTermGradeList(gradeQueryResult.getFirstTermGradeList());
                            result.setSecondTermGradeList(gradeQueryResult.getSecondTermGradeList());
                            result.setQueryYear(gradeQueryResult.getQueryYear());
                            break;

                        case EMPTY_RESULT:
                            //缓存无数据
                            result.setSuccess(false);
                            result.setEmpty(false);
                            result.setMessage("用户成绩信息未同步，请稍后再试");
                            break;

                        case ERROR_CONDITION:
                            //学期不可查询
                            result.setSuccess(false);
                            result.setEmpty(true);
                            result.setMessage("当前学期暂不可查询");
                            break;
                    }
                    break;

                case QUERY_ONLY:
                    //只查询教务系统
                    gradeQueryResult = gradeQueryService.QueryGradeData(request.getSession().getId()
                            , new User(username, password), year);
                    switch (gradeQueryResult.getGradeServiceResultEnum()) {
                        case SUCCESS:
                            //查询成功
                            result.setSuccess(true);
                            result.setFirstTermGPA(gradeQueryResult.getFirstTermGPA());
                            result.setFirstTermIGP(gradeQueryResult.getFirstTermIGP());
                            result.setSecondTermGPA(gradeQueryResult.getSecondTermGPA());
                            result.setSecondTermIGP(gradeQueryResult.getSecondTermIGP());
                            result.setFirstTermGradeList(gradeQueryResult.getFirstTermGradeList());
                            result.setSecondTermGradeList(gradeQueryResult.getSecondTermGradeList());
                            result.setQueryYear(gradeQueryResult.getQueryYear());
                            break;

                        case ERROR_CONDITION:
                            //学期不可查询
                            result.setSuccess(false);
                            result.setEmpty(true);
                            result.setMessage("当前学期暂不可查询");
                            break;

                        case PASSWORD_INCORRECT:
                            //用户密码错误
                            result.setSuccess(false);
                            result.setEmpty(false);
                            result.setMessage("密码已更新，请重新登录");
                            break;

                        case TIME_OUT:
                            //网络连接超时
                            result.setSuccess(false);
                            result.setEmpty(false);
                            result.setMessage("网络连接超时，请重试");
                            break;

                        case TIMESTAMP_INVALID:
                            //时间戳失效
                            result.setSuccess(false);
                            result.setEmpty(false);
                            result.setMessage("时间戳校验失败，请尝试重新登录");
                            break;

                        case SERVER_ERROR:
                        default:
                            //教务系统异常
                            result.setSuccess(false);
                            result.setEmpty(false);
                            result.setMessage("教务系统维护中，请稍后再试");
                            break;
                    }
                    break;

                default:
                    result.setSuccess(false);
                    result.setMessage("请求参数不合法");
                    break;
            }
        }
        return result;
    }
}
