package edu.gdei.gdeiassistant.Controller.Account.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class PasswordController {

    /**
     * 进入修改密码首页
     *
     * @return
     */
    @RequestMapping(value = "/password", method = RequestMethod.GET)
    public ModelAndView ResolvePasswordPage() {
        return new ModelAndView("Account/password");
    }
}
