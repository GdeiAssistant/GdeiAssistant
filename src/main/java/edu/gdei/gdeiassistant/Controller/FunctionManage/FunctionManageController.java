package edu.gdei.gdeiassistant.Controller.FunctionManage;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class FunctionManageController {

    @RequestMapping(value = "/function", method = RequestMethod.GET)
    public ModelAndView ResolveFunctionPage() {
        return new ModelAndView("Function/manage");
    }
}
