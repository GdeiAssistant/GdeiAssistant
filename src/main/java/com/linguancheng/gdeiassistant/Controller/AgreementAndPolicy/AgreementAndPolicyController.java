package com.linguancheng.gdeiassistant.Controller.AgreementAndPolicy;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class AgreementAndPolicyController {

    @RequestMapping(value = "/agreement", method = RequestMethod.GET)
    public ModelAndView ResolveAgreementPage() {
        return new ModelAndView("AgreementAndPolicy/agreement");
    }

    @RequestMapping(value = "/policy/cookie", method = RequestMethod.GET)
    public ModelAndView ResolveCookiePolicyPage() {
        return new ModelAndView("AgreementAndPolicy/cookiePolicy");
    }

    @RequestMapping(value = "/policy/privacy", method = RequestMethod.GET)
    public ModelAndView ResolvePrivacyPolicyPage() {
        return new ModelAndView("AgreementAndPolicy/privacyPolicy");
    }
}
