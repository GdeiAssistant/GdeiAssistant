package edu.gdei.gdeiassistant.Controller.Authenticate;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class AuthenticateController {

    /**
     * 进入实名认证页面
     *
     * @return
     */
    @RequestMapping(value = "/authentication", method = RequestMethod.GET)
    public ModelAndView ResolveAuthenticatePage() {
        return new ModelAndView("Authenticate/authenticate");
    }
}
