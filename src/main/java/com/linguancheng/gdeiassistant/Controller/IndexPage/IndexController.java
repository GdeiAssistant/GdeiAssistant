package com.linguancheng.gdeiassistant.Controller.IndexPage;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class IndexController {

    @RequestMapping(value = {"/index"}, method = RequestMethod.GET)
    public ModelAndView ResolveIndexPage() {
        return new ModelAndView("Index/index");
    }
}
