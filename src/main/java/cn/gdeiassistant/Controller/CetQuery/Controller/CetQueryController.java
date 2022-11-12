package cn.gdeiassistant.Controller.CetQuery.Controller;

import cn.gdeiassistant.Service.AcademicAffairs.CetQuery.CetQueryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;

@Controller
public class CetQueryController {

    @Autowired
    private CetQueryService cetQueryService;

    @RequestMapping(value = "/cet", method = RequestMethod.GET)
    public ModelAndView ResolveCetPage() {
        return new ModelAndView("Cet/cet");
    }

    @RequestMapping(value = "/cet/save", method = RequestMethod.GET)
    public ModelAndView ResolveCetNumberSavePage(HttpServletRequest request) throws Exception {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("Cet/cetSave");
        Long number = cetQueryService.getCetNumber(request.getSession().getId());
        modelAndView.addObject("CetNumber", number);
        return modelAndView;
    }

}
