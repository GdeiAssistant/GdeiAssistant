package com.linguancheng.gdeiassistant.Controller.CetQuery;

import com.linguancheng.gdeiassistant.Enum.Base.BoolResultEnum;
import com.linguancheng.gdeiassistant.Enum.Base.DataBaseResultEnum;
import com.linguancheng.gdeiassistant.Enum.Base.ServiceResultEnum;
import com.linguancheng.gdeiassistant.Pojo.CetQuery.CetQuery;
import com.linguancheng.gdeiassistant.Pojo.CetQuery.CetQueryJsonResult;
import com.linguancheng.gdeiassistant.Pojo.CetQuery.CetQueryResult;
import com.linguancheng.gdeiassistant.Pojo.Result.DataJsonResult;
import com.linguancheng.gdeiassistant.Pojo.Result.JsonResult;
import com.linguancheng.gdeiassistant.Pojo.Result.BaseResult;
import com.linguancheng.gdeiassistant.Service.CetQuery.CetQueryService;
import com.linguancheng.gdeiassistant.Tools.StringUtils;
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

/**
 * Created by linguancheng on 2017/7/21.
 */

@Controller
public class CetQueryController {

    @Autowired
    private CetQueryService cetQueryService;

    @RequestMapping(value = "/cet", method = RequestMethod.GET)
    public ModelAndView ResolveCetPage() {
        return new ModelAndView("Cet/cet");
    }

    @RequestMapping(value = {"/cet/checkcode", "/rest/cet/checkcode"}, method = RequestMethod.GET)
    @ResponseBody
    public DataJsonResult<String> GetCheckCodeImage(HttpServletRequest request) {
        DataJsonResult<String> dataJsonResult = new DataJsonResult<>();
        BaseResult<String, ServiceResultEnum> result = cetQueryService.CetIndex(request);
        switch (result.getResultType()) {
            case SUCCESS:
                dataJsonResult.setData(result.getResultData());
                dataJsonResult.setSuccess(true);
                break;

            case SERVER_ERROR:
                dataJsonResult.setSuccess(false);
                dataJsonResult.setMessage("学信网系统异常，获取验证码失败");
                break;

            case TIME_OUT:
                dataJsonResult.setSuccess(false);
                dataJsonResult.setMessage("网络连接超时，获取验证码失败");
                break;
        }
        return dataJsonResult;
    }

    @RequestMapping(value = "/cet/save", method = RequestMethod.GET)
    public ModelAndView ResolveCetNumberSavePage(HttpServletRequest request) {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("Cet/cetSave");
        String username = (String) WebUtils.getSessionAttribute(request, "username");
        BaseResult<Long, DataBaseResultEnum> result = cetQueryService.getCetNumber(username);
        switch (result.getResultType()) {
            case SUCCESS:
                modelAndView.addObject("CetNumber", result.getResultData());
                break;

            case ERROR:
                modelAndView.addObject("ErrorMessage", "系统维护中，请稍候再试");
                break;
        }
        return modelAndView;
    }

    @RequestMapping(value = "/cet/number", method = RequestMethod.GET)
    @ResponseBody
    public DataJsonResult<Long> GetCetNumber(HttpServletRequest request) {
        DataJsonResult<Long> dataJsonResult = new DataJsonResult<>();
        String username = (String) WebUtils.getSessionAttribute(request, "username");
        if (StringUtils.isBlank(username)) {
            dataJsonResult.setSuccess(false);
            dataJsonResult.setMessage("用户身份凭证已过期，请重新登录");
        } else {
            BaseResult<Long, DataBaseResultEnum> queryResult = cetQueryService.getCetNumber(username);
            switch (queryResult.getResultType()) {
                case SUCCESS:
                    dataJsonResult.setSuccess(true);
                    dataJsonResult.setData(queryResult.getResultData());
                    break;

                case ERROR:
                    dataJsonResult.setSuccess(false);
                    dataJsonResult.setMessage("系统维护中，请稍候再试");
                    break;

                case EMPTY_RESULT:
                    dataJsonResult.setSuccess(true);
                    dataJsonResult.setData(null);
                    break;
            }
        }
        return dataJsonResult;
    }

    @RequestMapping(value = "/cet/number", method = RequestMethod.POST)
    @ResponseBody
    public JsonResult SaveCetNumber(HttpServletRequest request, Long number) {
        JsonResult baseJsonResult = new JsonResult();
        String username = (String) WebUtils.getSessionAttribute(request, "username");
        if (StringUtils.isBlank(username)) {
            baseJsonResult.setSuccess(false);
            baseJsonResult.setMessage("用户身份凭证已过期，请重新登录");
        } else {
            BoolResultEnum resultEnum = cetQueryService.saveCetNumber(username, number);
            switch (resultEnum) {
                case SUCCESS:
                    baseJsonResult.setSuccess(true);
                    break;

                case ERROR:
                    baseJsonResult.setSuccess(false);
                    baseJsonResult.setMessage("系统维护中，请稍候再试");
                    break;
            }
        }
        return baseJsonResult;
    }

    @RequestMapping(value = "/rest/cetquery", method = RequestMethod.POST)
    @ResponseBody
    public CetQueryJsonResult CetQuery(HttpServletRequest request, @Validated CetQuery cetQuery, BindingResult bindingResult) {
        CetQueryJsonResult cetQueryJsonResult = new CetQueryJsonResult();
        if (bindingResult.hasErrors()) {
            cetQueryJsonResult.setSuccess(false);
            cetQueryJsonResult.setMessage("姓名或准考证号不合法");
        } else {
            CetQueryResult cetQueryResult = cetQueryService.CetQuery(request, cetQuery);
            switch (cetQueryResult.getCetQueryResultEnum()) {
                case SERVER_ERROR:
                    cetQueryJsonResult.setSuccess(false);
                    cetQueryJsonResult.setMessage("四六级查询系统维护中,请稍候再试");
                    break;

                case ERROR_CONDITION:
                    cetQueryJsonResult.setSuccess(false);
                    cetQueryJsonResult.setMessage("验证码信息错误，请重新输入");
                    break;

                case PASSWORD_INCORRECT:
                    cetQueryJsonResult.setSuccess(false);
                    cetQueryJsonResult.setMessage("无法找到对应的分数，请确认您输入的准考证号及姓名无误");
                    break;

                case TIME_OUT:
                    cetQueryJsonResult.setSuccess(false);
                    cetQueryJsonResult.setMessage("网络连接超时,请稍候再试");
                    break;

                case SUCCESS:
                    cetQueryJsonResult.setSuccess(true);
                    cetQueryJsonResult.setCet(cetQueryResult.getCet());
            }
        }
        return cetQueryJsonResult;
    }
}
