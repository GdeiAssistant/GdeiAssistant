package com.linguancheng.gdeiassistant.Controller.Evaluate;

import com.linguancheng.gdeiassistant.Annotation.RestAuthentication;
import com.linguancheng.gdeiassistant.Enum.Base.ServiceResultEnum;
import com.linguancheng.gdeiassistant.Pojo.Entity.User;
import com.linguancheng.gdeiassistant.Pojo.Result.JsonResult;
import com.linguancheng.gdeiassistant.Service.Evaluate.EvaluateService;
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

    @RequestMapping(value = "/evaluate", method = RequestMethod.GET)
    public ModelAndView ResolveTeacherEvaluatePage() {
        return new ModelAndView("Evaluate/evaluate");
    }

    @RequestMapping(value = "/evaluate", method = RequestMethod.POST)
    @ResponseBody
    public JsonResult StartEvaluate(HttpServletRequest request, boolean directlySubmit) throws Exception {
        JsonResult baseJsonResult = new JsonResult();
        String username = (String) WebUtils.getSessionAttribute(request, "username");
        String password = (String) WebUtils.getSessionAttribute(request, "password");
        ServiceResultEnum resultEnum = evaluateService.SyncSessionAndEvaluate(request.getSession().getId()
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
            , boolean directlySubmit) throws Exception {
        JsonResult baseJsonResult = new JsonResult();
        User user = (User) request.getAttribute("user");
        ServiceResultEnum resultEnum = evaluateService.SyncSessionAndEvaluate(request.getSession().getId()
                , user, directlySubmit);
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
