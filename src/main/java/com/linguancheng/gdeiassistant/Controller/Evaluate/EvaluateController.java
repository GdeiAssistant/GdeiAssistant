package com.linguancheng.gdeiassistant.Controller.Evaluate;

import com.linguancheng.gdeiassistant.Annotation.RestAuthentication;
import com.linguancheng.gdeiassistant.Enum.Base.ServiceResultEnum;
import com.linguancheng.gdeiassistant.Pojo.Entity.User;
import com.linguancheng.gdeiassistant.Pojo.Result.JsonResult;
import com.linguancheng.gdeiassistant.Service.Evaluate.EvaluateService;
import com.linguancheng.gdeiassistant.Service.UserLogin.UserLoginService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.util.WebUtils;

import javax.servlet.http.HttpServletRequest;

@Controller
public class EvaluateController {

    @Autowired
    private EvaluateService evaluateService;

    @Autowired
    private UserLoginService userLoginService;

    @RequestMapping(value = "/evaluate", method = RequestMethod.GET)
    public ModelAndView ResolveTeacherEvaluatePage() {
        return new ModelAndView("Evaluate/evaluate");
    }

    @RequestMapping(value = "/evaluate", method = RequestMethod.POST)
    @ResponseBody
    public JsonResult StartEvaluate(HttpServletRequest request, boolean directlySubmit) {
        JsonResult baseJsonResult = new JsonResult();
        String username = (String) WebUtils.getSessionAttribute(request, "username");
        String password = (String) WebUtils.getSessionAttribute(request, "password");
        //检测是否已与教务系统进行会话同步
        if (request.getSession().getAttribute("timestamp") == null) {
            //进行会话同步
            switch (userLoginService.SyncUpdateSession(request)) {
                case SUCCESS:
                    break;

                case TIME_OUT:
                    //连接超时
                    baseJsonResult.setSuccess(false);
                    baseJsonResult.setMessage("网络连接超时，请重试");
                    return baseJsonResult;

                case PASSWORD_INCORRECT:
                    //身份凭证异常
                    baseJsonResult.setSuccess(false);
                    baseJsonResult.setMessage("用户凭证已过期，请重新登录");
                    return baseJsonResult;

                default:
                    //服务器异常
                    baseJsonResult.setSuccess(false);
                    baseJsonResult.setMessage("学院教务系统维护中，暂不可用");
                    return baseJsonResult;
            }
        }
        ServiceResultEnum resultEnum = evaluateService.SyncSessionAndEvaluate(request
                , new User(username, password), directlySubmit);
        switch (resultEnum) {
            case SUCCESS:
                baseJsonResult.setSuccess(true);
                break;

            case TIME_OUT:
                baseJsonResult.setSuccess(false);
                baseJsonResult.setMessage("网络连接超时，请重试");
                break;

            case TIMESTAMP_INVALID:
                baseJsonResult.setSuccess(false);
                baseJsonResult.setMessage("时间戳校验失败，请尝试重新登录");
                break;

            case ERROR_CONDITION:
                baseJsonResult.setSuccess(false);
                baseJsonResult.setMessage("现在不是教学评价开放时间或你已完成教学评价");
                break;

            case SERVER_ERROR:
                baseJsonResult.setSuccess(false);
                baseJsonResult.setMessage("教务系统异常，请稍候再试");
                break;
        }
        return baseJsonResult;
    }

    @RequestMapping(value = "/rest/evaluate", method = RequestMethod.POST)
    @RestAuthentication
    @ResponseBody
    public JsonResult StartEvaluate(HttpServletRequest request, @RequestParam("token") String token
            , boolean directlySubmit) {
        JsonResult baseJsonResult = new JsonResult();
        User user = (User) request.getAttribute("user");
        ServiceResultEnum resultEnum = evaluateService.SyncSessionAndEvaluate(request, user, directlySubmit);
        switch (resultEnum) {
            case SUCCESS:
                baseJsonResult.setSuccess(true);
                break;

            case TIMESTAMP_INVALID:
                //时间戳校验失败
                baseJsonResult.setSuccess(false);
                baseJsonResult.setMessage("时间戳校验失败，请尝试重新登录");
                break;

            case TIME_OUT:
                baseJsonResult.setSuccess(false);
                baseJsonResult.setMessage("网络连接超时，请重试");
                break;

            case ERROR_CONDITION:
                baseJsonResult.setSuccess(false);
                baseJsonResult.setMessage("现在不是教学评价开放时间或你已完成教学评价");
                break;

            case SERVER_ERROR:
                baseJsonResult.setSuccess(false);
                baseJsonResult.setMessage("教务系统异常，请稍候再试");
                break;
        }
        return baseJsonResult;
    }
}
