package com.linguancheng.gdeiassistant.Controller.GradeQuery;

import com.linguancheng.gdeiassistant.Annotation.QueryLog;
import com.linguancheng.gdeiassistant.Annotation.RestQueryLog;
import com.linguancheng.gdeiassistant.Enum.Base.LoginResultEnum;
import com.linguancheng.gdeiassistant.Pojo.Document.GradeDocument;
import com.linguancheng.gdeiassistant.Pojo.Entity.Grade;
import com.linguancheng.gdeiassistant.Pojo.Entity.User;
import com.linguancheng.gdeiassistant.Pojo.GradeQuery.GradeQueryJsonResult;
import com.linguancheng.gdeiassistant.Pojo.GradeQuery.GradeQueryResult;
import com.linguancheng.gdeiassistant.Pojo.Result.BaseResult;
import com.linguancheng.gdeiassistant.Service.GradeQuery.GradeCacheService;
import com.linguancheng.gdeiassistant.Service.GradeQuery.GradeQueryService;
import com.linguancheng.gdeiassistant.Service.UserLogin.UserLoginService;
import com.linguancheng.gdeiassistant.ValidGroup.User.ServiceQueryValidGroup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by linguancheng on 2017/7/22.
 */

@Controller
public class GradeQueryController {

    @Autowired
    private GradeQueryService gradeQueryService;

    @Autowired
    private GradeCacheService gradeCacheService;

    @Autowired
    private UserLoginService userLoginService;

    @RequestMapping(value = "/grade")
    public ModelAndView ResolveGradePage() {
        return new ModelAndView("Grade/grade");
    }

    /**
     * 成绩查询Rest接口
     *
     * @param request
     * @param user
     * @param year
     * @param bindingResult
     * @return
     */
    @RequestMapping(value = "/rest/gradequery", method = RequestMethod.POST)
    @RestQueryLog
    @ResponseBody
    public GradeQueryJsonResult GradeQuery(HttpServletRequest request, @Validated(value = ServiceQueryValidGroup.class) User user
            , @RequestParam(value = "year", required = false) Integer year
            , @RequestParam(value = "refresh", required = false, defaultValue = "false") Boolean refresh
            , BindingResult bindingResult) {
        GradeQueryJsonResult result = new GradeQueryJsonResult();
        if (year != null && (year < 0 || year > 3) || bindingResult.hasErrors()) {
            result.setSuccess(false);
            result.setErrorMessage("请求参数不合法");
        } else {
            String username = user.getUsername();
            String keycode = user.getKeycode();
            String number = user.getNumber();
            //校验用户账号身份
            BaseResult<User, LoginResultEnum> loginResult = userLoginService.UserLogin(request, user, true);
            switch (loginResult.getResultType()) {
                case LOGIN_SUCCESS:
                    //优先查询缓存数据
                    if (!refresh) {
                        GradeDocument gradeDocument = gradeCacheService.ReadGrade(username);
                        if (gradeDocument != null) {
                            List<GradeDocument.GradeList> gradeLists = gradeDocument.getGradeList();
                            if (year == null) {
                                year = gradeLists.size() - 1;
                            }
                            if (gradeLists.size() == 0 || year >= gradeLists.size()) {
                                result.setSuccess(false);
                                result.setErrorMessage("当前学年暂不可以查询");
                                return result;
                            }
                            List<Grade> gradeList = gradeDocument.getGradeList().get(year).getGradeList();
                            List<Grade> firstTermGradeList = new ArrayList<>();
                            List<Grade> secondTermGradeList = new ArrayList<>();
                            for (Grade grade : gradeList) {
                                if (grade.getGrade_term().equals("1")) {
                                    firstTermGradeList.add(grade);
                                } else {
                                    secondTermGradeList.add(grade);
                                }
                            }
                            Double firstTermGPA = gradeDocument.getFirstTermGPAList().get(year);
                            Double secondTermGPA = gradeDocument.getSecondTermGPAList().get(year);
                            Double firstTermIGP = gradeDocument.getFirstTermIGPList().get(year);
                            Double secondTermIGP = gradeDocument.getSecondTermIGPList().get(year);
                            result.setSuccess(true);
                            result.setFirstTermGPA(firstTermGPA);
                            result.setFirstTermIGP(firstTermIGP);
                            result.setSecondTermGPA(secondTermGPA);
                            result.setSecondTermIGP(secondTermIGP);
                            result.setFirstTermGradeList(firstTermGradeList);
                            result.setSecondTermGradeList(secondTermGradeList);
                            result.setQueryYear(year);
                            result.setCurrentTerm(gradeQueryService.getCurrentTerm());
                            return result;
                        }
                    }
                    //若缓存数据不存在或要求强制更新，则从教务系统获取
                    if (year == null) {
                        //若没有指定查询的学年，则进行默认学年查询
                        year = -1;
                    }
                    GradeQueryResult gradeQueryResult = gradeQueryService.GradeQuery(request, username
                            , keycode, number, year);
                    switch (gradeQueryResult.getGradeServiceResultEnum()) {
                        case PASSWORD_INCORRECT:
                            //身份凭证异常
                            result.setSuccess(false);
                            result.setEmpty(false);
                            result.setErrorMessage("密码错误，请重新登录");
                            break;

                        case SERVER_ERROR:
                            //服务器异常
                            result.setSuccess(false);
                            result.setEmpty(false);
                            result.setErrorMessage("学院教务系统维护中,暂不可用");
                            break;

                        case TIME_OUT:
                            //连接超时
                            result.setSuccess(false);
                            result.setEmpty(false);
                            result.setErrorMessage("网络连接超时，请重试");
                            break;

                        case ERROR_CONDITION:
                            //当前学年暂不可用
                            result.setSuccess(false);
                            result.setEmpty(true);
                            result.setErrorMessage("当前学年暂不可以查询");
                            break;

                        case SUCCESS:
                            //查询成功
                            result.setSuccess(true);
                            result.setFirstTermGradeList(gradeQueryResult.getFirstTermGradeList());
                            result.setFirstTermGPA(gradeQueryResult.getFirstTermGPA());
                            result.setFirstTermIGP(gradeQueryResult.getFirstTermIGP());
                            result.setSecondTermGradeList(gradeQueryResult.getSecondTermGradeList());
                            result.setSecondTermGPA(gradeQueryResult.getSecondTermGPA());
                            result.setSecondTermIGP(gradeQueryResult.getSecondTermIGP());
                            result.setQueryYear(gradeQueryResult.getQueryYear());
                            result.setCurrentTerm(gradeQueryService.getCurrentTerm());
                            break;
                    }
                    break;

                case PASSWORD_ERROR:
                    //用户名或密码错误
                    result.setSuccess(false);
                    result.setEmpty(false);
                    result.setErrorMessage("密码已更新，请重新登录");
                    break;

                case TIME_OUT:
                    //连接超时
                    result.setSuccess(false);
                    result.setEmpty(false);
                    result.setErrorMessage("网络连接超时，请稍候再试");
                    break;

                case SERVER_ERROR:
                    //服务器异常
                    result.setSuccess(false);
                    result.setEmpty(false);
                    result.setErrorMessage("教务系统维护中，请稍候再试");
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
     * @return
     */
    @RequestMapping(value = "/gradequery", method = RequestMethod.POST)
    @QueryLog
    @ResponseBody
    public GradeQueryJsonResult GradeQuery(HttpServletRequest request
            , @RequestParam(value = "year", required = false) Integer year
            , @RequestParam(value = "refresh", required = false, defaultValue = "false") Boolean refresh) {
        GradeQueryJsonResult gradeQueryJsonResult = new GradeQueryJsonResult();
        if (year != null && (year < 0 || year > 3)) {
            gradeQueryJsonResult.setSuccess(false);
            gradeQueryJsonResult.setErrorMessage("请求参数不合法");
        } else {
            String username = (String) request.getSession().getAttribute("username");
            String keycode = (String) request.getSession().getAttribute("keycode");
            String number = (String) request.getSession().getAttribute("number");
            //优先查询缓存数据
            if (!refresh) {
                GradeDocument gradeDocument = gradeCacheService.ReadGrade(username);
                if (gradeDocument != null) {
                    List<GradeDocument.GradeList> gradeLists = gradeDocument.getGradeList();
                    if (year == null) {
                        //若没有指定查询的学年，则进行默认学年查询
                        year = gradeLists.size() - 1;
                    }
                    if (year >= gradeLists.size()) {
                        gradeQueryJsonResult.setSuccess(false);
                        gradeQueryJsonResult.setErrorMessage("当前学年暂不可以查询");
                        return gradeQueryJsonResult;
                    }
                    List<Grade> gradeList = gradeDocument.getGradeList().get(year).getGradeList();
                    List<Grade> firstTermGradeList = new ArrayList<>();
                    List<Grade> secondTermGradeList = new ArrayList<>();
                    for (Grade grade : gradeList) {
                        if (grade.getGrade_term().equals("1")) {
                            firstTermGradeList.add(grade);
                        } else {
                            secondTermGradeList.add(grade);
                        }
                    }
                    Double firstTermGPA = gradeDocument.getFirstTermGPAList().get(year);
                    Double secondTermGPA = gradeDocument.getSecondTermGPAList().get(year);
                    Double firstTermIGP = gradeDocument.getFirstTermIGPList().get(year);
                    Double secondTermIGP = gradeDocument.getSecondTermIGPList().get(year);
                    gradeQueryJsonResult.setSuccess(true);
                    gradeQueryJsonResult.setFirstTermGPA(firstTermGPA);
                    gradeQueryJsonResult.setFirstTermIGP(firstTermIGP);
                    gradeQueryJsonResult.setSecondTermGPA(secondTermGPA);
                    gradeQueryJsonResult.setSecondTermIGP(secondTermIGP);
                    gradeQueryJsonResult.setFirstTermGradeList(firstTermGradeList);
                    gradeQueryJsonResult.setSecondTermGradeList(secondTermGradeList);
                    gradeQueryJsonResult.setQueryYear(year);
                    gradeQueryJsonResult.setCurrentTerm(gradeQueryService.getCurrentTerm());
                    return gradeQueryJsonResult;
                }
                //若缓存不存在或要求强制更新，则从教务系统获取
                if (year == null) {
                    //若没有指定查询的学年，则进行默认学年查询
                    year = -1;
                }
                GradeQueryResult gradeQueryResult = gradeQueryService.GradeQuery(request, username, keycode, number, year);
                switch (gradeQueryResult.getGradeServiceResultEnum()) {
                    case PASSWORD_INCORRECT:
                        //身份凭证异常
                        gradeQueryJsonResult.setSuccess(false);
                        gradeQueryJsonResult.setErrorMessage("用户凭证已过期，请重新登录");
                        break;

                    case SERVER_ERROR:
                        //服务器异常
                        gradeQueryJsonResult.setSuccess(false);
                        gradeQueryJsonResult.setErrorMessage("学院教务系统维护中，暂不可用");
                        break;

                    case TIME_OUT:
                        //连接超时
                        gradeQueryJsonResult.setSuccess(false);
                        gradeQueryJsonResult.setErrorMessage("网络连接超时，请重试");
                        break;

                    case ERROR_CONDITION:
                        //当前学年暂不可用
                        gradeQueryJsonResult.setSuccess(false);
                        gradeQueryJsonResult.setErrorMessage("当前学年暂不可以查询");
                        break;

                    case SUCCESS:
                        //查询成功
                        gradeQueryJsonResult.setSuccess(true);
                        gradeQueryJsonResult.setQueryYear(gradeQueryResult.getQueryYear());
                        gradeQueryJsonResult.setFirstTermGradeList(gradeQueryResult.getFirstTermGradeList());
                        gradeQueryJsonResult.setFirstTermGPA(gradeQueryResult.getFirstTermGPA());
                        gradeQueryJsonResult.setFirstTermIGP(gradeQueryResult.getFirstTermIGP());
                        gradeQueryJsonResult.setSecondTermGradeList(gradeQueryResult.getSecondTermGradeList());
                        gradeQueryJsonResult.setSecondTermGPA(gradeQueryResult.getSecondTermGPA());
                        gradeQueryJsonResult.setSecondTermIGP(gradeQueryResult.getSecondTermIGP());
                        gradeQueryJsonResult.setQueryYear(gradeQueryJsonResult.getQueryYear());
                        gradeQueryJsonResult.setCurrentTerm(gradeQueryService.getCurrentTerm());
                        break;
                }
            }
        }
        return gradeQueryJsonResult;
    }
}
