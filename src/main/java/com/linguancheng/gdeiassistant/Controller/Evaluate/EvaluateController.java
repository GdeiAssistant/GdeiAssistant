package com.gdeiassistant.gdeiassistant.Controller.Evaluate;

import com.gdeiassistant.gdeiassistant.Enum.Base.ServiceResultEnum;
import com.gdeiassistant.gdeiassistant.Pojo.Entity.User;
import com.gdeiassistant.gdeiassistant.Pojo.Result.BaseJsonResult;
import com.gdeiassistant.gdeiassistant.Service.Evaluate.EvaluateService;
import com.gdeiassistant.gdeiassistant.Tools.StringUtils;
import com.gdeiassistant.gdeiassistant.ValidGroup.User.ServiceQueryValidGroup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
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

    @RequestMapping(value = "/evaluate", method = RequestMethod.GET)
    public ModelAndView ResolveTeacherEvaluatePage() {
        return new ModelAndView("Evaluate/evaluate");
    }

    @RequestMapping(value = "/evaluate", method = RequestMethod.POST)
    @ResponseBody
    public BaseJsonResult StartEvaluate(HttpServletRequest request, boolean directlySubmit) {
        BaseJsonResult baseJsonResult = new BaseJsonResult();
        String username = (String) WebUtils.getSessionAttribute(request, "username");
        String keycode = (String) WebUtils.getSessionAttribute(request, "keycode");
        String number = (String) WebUtils.getSessionAttribute(request, "number");
        if (!StringUtils.isBlank(username) && !StringUtils.isBlank(keycode) && !StringUtils.isBlank(number)) {
            ServiceResultEnum resultEnum = evaluateService.TeacherEvaluate(request, username, keycode, number, directlySubmit);
            switch (resultEnum) {
                case SUCCESS:
                    baseJsonResult.setSuccess(true);
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
        } else {
            baseJsonResult.setSuccess(false);
            baseJsonResult.setErrorMessage("用户身份凭证已过期，请重新登录");
        }
        return baseJsonResult;
    }

    @RequestMapping(value = "/rest/evaluate", method = RequestMethod.POST)
    @ResponseBody
    public BaseJsonResult StartEvaluate(HttpServletRequest request, @Validated(value = ServiceQueryValidGroup.class) User user
            , BindingResult bindingResult, boolean directlySubmit) {
        BaseJsonResult baseJsonResult = new BaseJsonResult();
        if (bindingResult.hasErrors()) {
            baseJsonResult.setSuccess(false);
            baseJsonResult.setErrorMessage("请求参数不合法");
            return baseJsonResult;
        }
        ServiceResultEnum resultEnum = evaluateService.TeacherEvaluate(request, user.getUsername()
                , user.getKeycode(), user.getNumber(), directlySubmit);
        switch (resultEnum) {
            case SUCCESS:
                baseJsonResult.setSuccess(true);
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
