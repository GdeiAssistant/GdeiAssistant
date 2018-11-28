package com.linguancheng.gdeiassistant.Controller.Evaluate;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class EvaluateController {

    @RequestMapping(value = "/evaluate", method = RequestMethod.GET)
    public ModelAndView ResolveTeacherEvaluatePage() {
        return new ModelAndView("Evaluate/evaluate");
    }
}
