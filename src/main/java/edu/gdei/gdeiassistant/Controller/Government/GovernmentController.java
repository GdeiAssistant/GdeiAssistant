package edu.gdei.gdeiassistant.Controller.Government;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class GovernmentController {

    /**
     * 进入政务服务首页
     *
     * @return
     */
    @RequestMapping(value = "/government", method = RequestMethod.GET)
    public ModelAndView ResolveGovernmentPage() {
        return new ModelAndView("Government/index");
    }
}
