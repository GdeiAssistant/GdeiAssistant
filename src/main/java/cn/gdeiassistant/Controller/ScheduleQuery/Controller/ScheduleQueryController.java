package cn.gdeiassistant.Controller.ScheduleQuery.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
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
