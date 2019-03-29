package edu.gdei.gdeiassistant.Controller.KaoYan;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class KaoYanController {

    @RequestMapping(value = "/kaoyan", method = RequestMethod.GET)
    public ModelAndView ResolveKaoYanPage() {
        return new ModelAndView("KaoYan/kaoyan");
    }
}
