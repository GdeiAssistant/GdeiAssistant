package edu.gdei.gdeiassistant.Controller.Express.Controller;

import edu.gdei.gdeiassistant.Annotation.CheckAuthentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;

@Controller
public class ExpressController {

    /**
     * 进入表白墙首页
     *
     * @return
     */
    @RequestMapping(value = "/express", method = RequestMethod.GET)
    @CheckAuthentication(name = "express")
    public ModelAndView ResolveExpressPage(HttpServletRequest request) {
        return new ModelAndView("Express/index");
    }

    /**
     * 进入表白墙发布页
     *
     * @return
     */
    @RequestMapping(value = "/express/publish", method = RequestMethod.GET)
    @CheckAuthentication(name = "express")
    public ModelAndView ResolveExpressPublishPage(HttpServletRequest request) {
        return new ModelAndView("Express/publish");
    }

    /**
     * 进入表白墙搜索页
     *
     * @return
     */
    @RequestMapping(value = "/express/search", method = RequestMethod.GET)
    @CheckAuthentication(name = "express")
    public ModelAndView ResolveExpressSearchPage(HttpServletRequest request) {
        return new ModelAndView("Express/search");
    }
}
