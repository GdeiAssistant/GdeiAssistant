package com.linguancheng.gdeiassistant.Controller.CloseAccount;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

/**
 * 广东二师助手团队 林冠成 版权所有
 * All rights reserved © 2016 - 2018
 * Author:林冠成
 * Date:2018/9/25
 */
@Controller
public class CloseAccountController {

    /**
     * 进入账号删除页面
     *
     * @return
     */
    @RequestMapping(value = "/close", method = RequestMethod.GET)
    public ModelAndView ResolveClosePage() {
        return new ModelAndView("Close/closeAccount");
    }
}
