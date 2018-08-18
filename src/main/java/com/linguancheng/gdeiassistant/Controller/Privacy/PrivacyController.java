package com.linguancheng.gdeiassistant.Controller.Privacy;

import com.linguancheng.gdeiassistant.Enum.Base.DataBaseResultEnum;
import com.linguancheng.gdeiassistant.Pojo.Entity.Privacy;
import com.linguancheng.gdeiassistant.Pojo.Result.BaseResult;
import com.linguancheng.gdeiassistant.Pojo.Result.DataJsonResult;
import com.linguancheng.gdeiassistant.Service.Privacy.PrivacyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;

@Controller
public class PrivacyController {

    @Autowired
    private PrivacyService privacyService;

    /**
     * 进入隐私设置界面
     *
     * @return
     */
    @RequestMapping(value = {"/privacy", "/yiban/privacy"}, method = RequestMethod.GET)
    public ModelAndView ResolvePrivacySettingPage() {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("Profile/privacy");
        return modelAndView;
    }

    /**
     * 获取用户隐私设置
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/rest/privacy", method = RequestMethod.GET)
    @ResponseBody
    public DataJsonResult<Privacy> GetUserPrivacySetting(HttpServletRequest request) {
        DataJsonResult<Privacy> result = new DataJsonResult<>();
        String username = (String) request.getSession().getAttribute("username");
        if (username == null || username.trim().isEmpty()) {
            result.setSuccess(false);
            result.setErrorMessage("用户身份凭证过期，请稍候再试");
        } else {
            BaseResult<Privacy, DataBaseResultEnum> queryResult = privacyService.GetPrivacySetting(username);
            switch (queryResult.getResultType()) {
                case SUCCESS:
                    result.setSuccess(true);
                    result.setData(queryResult.getResultData());
                    break;

                case EMPTY_RESULT:
                    result.setSuccess(false);
                    result.setErrorMessage("用户隐私设置未初始化，请重新登录系统");
                    break;

                case ERROR:
                    result.setSuccess(false);
                    result.setErrorMessage("服务器异常，请稍候再试");
                    break;
            }
        }
        return result;
    }

    /**
     * 更新用户隐私设置
     *
     * @param request
     * @param index
     * @param state
     * @return
     */
    @RequestMapping(value = "/rest/privacy", method = RequestMethod.POST)
    @ResponseBody
    public DataJsonResult<Privacy> UpdateUserPrivacySetting(HttpServletRequest request, int index, boolean state) {
        DataJsonResult<Privacy> result = new DataJsonResult<>();
        String username = (String) request.getSession().getAttribute("username");
        if (username == null || username.trim().isEmpty()) {
            result.setSuccess(false);
            result.setErrorMessage("用户身份凭证过期，请稍候再试");
        } else {
            switch (index) {
                case 0:
                    if (privacyService.UpdateGender(state, username)) {
                        result.setSuccess(true);
                    } else {
                        result.setSuccess(false);
                        result.setErrorMessage("服务器异常，请稍候再试");
                    }
                    break;

                case 1:
                    if (privacyService.UpdateGenderOrientation(state, username)) {
                        result.setSuccess(true);
                    } else {
                        result.setSuccess(false);
                        result.setErrorMessage("服务器异常，请稍候再试");
                    }
                    break;

                case 2:
                    if (privacyService.UpdateLocation(state, username)) {
                        result.setSuccess(true);
                    } else {
                        result.setSuccess(false);
                        result.setErrorMessage("服务器异常，请稍候再试");
                    }
                    break;

                case 3:
                    if (privacyService.UpdateIntroduction(state, username)) {
                        result.setSuccess(true);
                    } else {
                        result.setSuccess(false);
                        result.setErrorMessage("服务器异常，请稍候再试");
                    }
                    break;

                case 4:
                    if (privacyService.UpdateCache(state, username)) {
                        result.setSuccess(true);
                    } else {
                        result.setSuccess(false);
                        result.setErrorMessage("服务器异常，请稍候再试");
                    }
                    break;

                default:
                    result.setSuccess(false);
                    result.setErrorMessage("请求参数不合法");
                    break;
            }
        }
        return result;
    }
}
