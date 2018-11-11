package com.linguancheng.gdeiassistant.Controller.CloseAccount;

import com.linguancheng.gdeiassistant.Exception.CommonException.TransactionException;
import com.linguancheng.gdeiassistant.Pojo.Result.JsonResult;
import com.linguancheng.gdeiassistant.Service.CloseAccount.CloseAccountService;
import com.linguancheng.gdeiassistant.Tools.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;

/**
 * 广东二师助手团队 林冠成 版权所有
 * All rights reserved © 2016 - 2018
 * Author:林冠成
 * Date:2018/9/25
 */
@Controller
public class CloseAccountController {

    @Autowired
    private CloseAccountService closeAccountService;

    /**
     * 进入账号删除页面
     *
     * @return
     */
    @RequestMapping(value = "/close", method = RequestMethod.GET)
    public ModelAndView ResolveClosePage() {
        return new ModelAndView("Close/closeAccount");
    }

    /**
     * 删除账号
     *
     * @param request
     * @param password
     * @return
     */
    @RequestMapping(value = "/close/submit", method = RequestMethod.POST)
    @ResponseBody
    public JsonResult CloseAccount(HttpServletRequest request, String password) {
        JsonResult baseJsonResult = new JsonResult();
        String username = (String) request.getSession().getAttribute("username");
        if (StringUtils.isNotBlank(username)) {
            try {
                closeAccountService.CloseAccount(username, password);
                baseJsonResult.setSuccess(true);
            } catch (TransactionException e) {
                baseJsonResult.setSuccess(false);
                baseJsonResult.setMessage(e.getMessage());
            }
        } else {
            baseJsonResult.setSuccess(false);
            baseJsonResult.setMessage("登录凭证已过期，请重新登录");
        }
        return baseJsonResult;
    }
}
