package edu.gdei.gdeiassistant.Controller.Account;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class GraduatedAccountController {

    @RequestMapping(value = "/graduation", method = RequestMethod.GET)
    public ModelAndView ResolveGraduationPage() {
        return new ModelAndView("Account/graduatedAccount");
    }
}
