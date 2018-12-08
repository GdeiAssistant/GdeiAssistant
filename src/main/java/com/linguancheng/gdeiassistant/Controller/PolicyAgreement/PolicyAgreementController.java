package com.linguancheng.gdeiassistant.Controller.PolicyAgreement;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class PolicyAgreementController {

    @RequestMapping(value = "/agreement", method = RequestMethod.GET)
    public ModelAndView ResolveAgreementPage() {
        return new ModelAndView("PolicyAgreement/agreement");
    }

    @RequestMapping(value = "/policy", method = RequestMethod.GET)
    public ModelAndView ResolvePolicyPage() {
        return new ModelAndView("PolicyAgreement/policy");
    }
}
