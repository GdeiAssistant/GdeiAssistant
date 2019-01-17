package com.linguancheng.gdeiassistant.Controller.About;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class AboutController {

    @RequestMapping(value = "/about", method = RequestMethod.GET)
    public ModelAndView ResolveAboutPage() {
        return new ModelAndView("About/index");
    }

    @RequestMapping(value = "/agreement", method = RequestMethod.GET)
    public ModelAndView ResolveAgreementPage() {
        return new ModelAndView("About/agreement");
    }

    @RequestMapping(value = "/policy/cookie", method = RequestMethod.GET)
    public ModelAndView ResolveCookiePolicyPage() {
        return new ModelAndView("About/cookiePolicy");
    }

    @RequestMapping(value = "/policy/privacy", method = RequestMethod.GET)
    public ModelAndView ResolvePrivacyPolicyPage() {
        return new ModelAndView("About/privacyPolicy");
    }

    @RequestMapping(value = "/policy/social", method = RequestMethod.GET)
    public ModelAndView ResolveSocialPolicyPage() {
        return new ModelAndView("About/socialPolicy");
    }
}
