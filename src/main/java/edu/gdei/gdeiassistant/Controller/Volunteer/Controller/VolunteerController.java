package edu.gdei.gdeiassistant.Controller.Volunteer.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class VolunteerController {

    /**
     * 进入志愿活动主页
     *
     * @return
     */
    @RequestMapping(value = "/volunteer", method = RequestMethod.GET)
    public ModelAndView ResolveVolunteerPage() {
        return new ModelAndView("/Volunteer/volunteer");
    }
}
