package edu.gdei.gdeiassistant.Controller.ScheduleQuery;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class ScheduleQueryController {

    /**
     * 进入课表查询页面
     *
     * @return
     */
    @RequestMapping(value = "schedule", method = RequestMethod.GET)
    public ModelAndView ResolveSchedulePage() {
        return new ModelAndView("Schedule/schedule");
    }
}
