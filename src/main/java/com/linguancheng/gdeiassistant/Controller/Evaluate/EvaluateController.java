package com.linguancheng.gdeiassistant.Controller.Evaluate;

import com.linguancheng.gdeiassistant.Enum.Base.ServiceResultEnum;
import com.linguancheng.gdeiassistant.Pojo.Entity.User;
import com.linguancheng.gdeiassistant.Pojo.Result.BaseJsonResult;
import com.linguancheng.gdeiassistant.Pojo.UserLogin.UserLoginResult;
import com.linguancheng.gdeiassistant.Service.Evaluate.EvaluateService;
import com.linguancheng.gdeiassistant.Service.UserLogin.UserLoginService;
import com.linguancheng.gdeiassistant.Tools.StringUtils;
import com.linguancheng.gdeiassistant.ValidGroup.User.UserLoginValidGroup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
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
    public BaseJsonResult StartEvaluate(HttpServletRequest request, boolean directlySubmit) {
        BaseJsonResult baseJsonResult = new BaseJsonResult();
        String username = (String) WebUtils.getSessionAttribute(request, "username");
        //检测是否已与教务系统进行会话同步
        if (request.getSession().getAttribute("timestamp") != null) {
            //进行会话同步
            switch (userLoginService.SyncUpdateSession(request)) {
                case SUCCESS:
                    break;

                case TIME_OUT:
                    //连接超时
                    baseJsonResult.setSuccess(false);
                    baseJsonResult.setErrorMessage("网络连接超时，请重试");
                    return baseJsonResult;

                case PASSWORD_INCORRECT:
                    //身份凭证异常
                    baseJsonResult.setSuccess(false);
                    baseJsonResult.setErrorMessage("用户凭证已过期，请重新登录");
                    return baseJsonResult;

                default:
                    //服务器异常
                    baseJsonResult.setSuccess(false);
                    baseJsonResult.setErrorMessage("学院教务系统维护中，暂不可用");
                    return baseJsonResult;
            }
        }
        String keycode = (String) WebUtils.getSessionAttribute(request, "keycode");
        String number = (String) WebUtils.getSessionAttribute(request, "number");
        Long timestamp = (Long) WebUtils.getSessionAttribute(request, "timestamp");
        ServiceResultEnum resultEnum = evaluateService
                .TeacherEvaluate(request, username, keycode, number, timestamp, directlySubmit);
        switch (resultEnum) {
            case SUCCESS:
                baseJsonResult.setSuccess(true);
                break;

            case TIME_OUT:
                baseJsonResult.setSuccess(false);
                baseJsonResult.setErrorMessage("网络连接超时，请重试");
                break;

            case TIMESTAMP_INVALID:
                baseJsonResult.setSuccess(false);
                baseJsonResult.setErrorMessage("时间戳校验失败，请尝试重新登录");
                break;

            case ERROR_CONDITION:
                baseJsonResult.setSuccess(false);
                baseJsonResult.setErrorMessage("现在不是教学评价开放时间或你已完成教学评价");
                break;

            case SERVER_ERROR:
                baseJsonResult.setSuccess(false);
                baseJsonResult.setErrorMessage("教务系统异常，请稍候再试");
                break;
        }
        return baseJsonResult;
    }

    @RequestMapping(value = "/rest/evaluate", method = RequestMethod.POST)
    @ResponseBody
    public BaseJsonResult StartEvaluate(HttpServletRequest request
            , @ModelAttribute("user") @Validated(value = UserLoginValidGroup.class) User user
            , BindingResult bindingResult, Long timestamp, boolean directlySubmit) {
        BaseJsonResult baseJsonResult = new BaseJsonResult();
        if (bindingResult.hasErrors()) {
            baseJsonResult.setSuccess(false);
            baseJsonResult.setErrorMessage("请求参数不合法");
            return baseJsonResult;
        }
        //检测是否已与教务系统进行会话同步
        if (timestamp == null) {
            //进行会话同步
            UserLoginResult userLoginResult = userLoginService.UserLogin(request,
                    user, false);
            switch (userLoginResult.getLoginResultEnum()) {
                case LOGIN_SUCCESS:
                    timestamp = userLoginResult.getTimestamp();
                    if (StringUtils.isBlank(user.getKeycode())) {
                        user.setKeycode(userLoginResult.getUser().getKeycode());
                    }
                    if (StringUtils.isBlank(user.getNumber())) {
                        user.setNumber(userLoginResult.getUser().getNumber());
                    }
                    break;

                case SERVER_ERROR:
                    //服务器异常
                    baseJsonResult.setSuccess(false);
                    baseJsonResult.setErrorMessage("教务系统异常，请稍候再试");
                    return baseJsonResult;

                case TIME_OUT:
                    //连接超时
                    baseJsonResult.setSuccess(false);
                    baseJsonResult.setErrorMessage("网络连接超时，请重试");
                    return baseJsonResult;

                case PASSWORD_ERROR:
                    //用户名或密码错误
                    baseJsonResult.setSuccess(false);
                    baseJsonResult.setErrorMessage("密码已更新，请重新登录");
                    return baseJsonResult;
            }
        }
        ServiceResultEnum resultEnum = evaluateService.TeacherEvaluate(request, user.getUsername()
                , user.getKeycode(), user.getNumber(), timestamp, directlySubmit);
        switch (resultEnum) {
            case SUCCESS:
                baseJsonResult.setSuccess(true);
                break;

            case TIMESTAMP_INVALID:
                //时间戳校验失败
                baseJsonResult.setSuccess(false);
                baseJsonResult.setErrorMessage("时间戳校验失败，请尝试重新登录");
                break;

            case TIME_OUT:
                baseJsonResult.setSuccess(false);
                baseJsonResult.setErrorMessage("网络连接超时，请重试");
                break;

            case ERROR_CONDITION:
                baseJsonResult.setSuccess(false);
                baseJsonResult.setErrorMessage("现在不是教学评价开放时间或你已完成教学评价");
                break;

            case SERVER_ERROR:
                baseJsonResult.setSuccess(false);
                baseJsonResult.setErrorMessage("教务系统异常，请稍候再试");
                break;
        }
        return baseJsonResult;
    }
}
