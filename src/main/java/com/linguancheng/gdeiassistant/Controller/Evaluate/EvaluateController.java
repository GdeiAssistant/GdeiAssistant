package com.linguancheng.gdeiassistant.Controller.Evaluate;

import com.linguancheng.gdeiassistant.Enum.Base.ServiceResultEnum;
import com.linguancheng.gdeiassistant.Pojo.Result.BaseJsonResult;
import com.linguancheng.gdeiassistant.Service.Evaluate.EvaluateService;
import com.linguancheng.gdeiassistant.Tools.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
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

    @RequestMapping("/evaluate")
    public ModelAndView ResolveTeacherEvaluatePage() {
        return new ModelAndView("Evaluate/evaluate");
    }

    @RequestMapping(value = "/rest/evaluate", method = RequestMethod.POST)
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

}
