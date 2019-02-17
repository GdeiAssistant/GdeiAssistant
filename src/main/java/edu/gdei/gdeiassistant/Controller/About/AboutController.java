package edu.gdei.gdeiassistant.Controller.About;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class AboutController {

    @RequestMapping(value = {"/", "/about"}, method = RequestMethod.GET)
    public ModelAndView ResolveAboutPage() {
        return new ModelAndView("About/index");
    }

    @RequestMapping(value = "/about/security", method = RequestMethod.GET)
    public ModelAndView ResolveAboutSecurityPage() {
        return new ModelAndView("About/security");
    }

    @RequestMapping(value = "/about/account", method = RequestMethod.GET)
    public ModelAndView ResolveAboutAccountPage() {
        return new ModelAndView("About/account");
    }

    @RequestMapping(value = "/about/wechat", method = RequestMethod.GET)
    public ModelAndView ResolveAboutWechatPage() {
        return new ModelAndView("About/wechat");
    }

    @RequestMapping(value = "/license", method = RequestMethod.GET)
    public ModelAndView ResolveLicensePage() {
        return new ModelAndView("About/license");
    }

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

    @RequestMapping(value = "/policy/social", method = RequestMethod.GET)
    public ModelAndView ResolveSocialPolicyPage() {
        return new ModelAndView("AgreementAndPolicy/socialPolicy");
    }
}
