package edu.gdei.gdeiassistant.Controller.GradeQuery;

import edu.gdei.gdeiassistant.Service.GradeQuery.GradeQueryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class GradeQueryController {

    @Autowired
    private GradeQueryService gradeQueryService;

    @RequestMapping(value = "/grade")
    public ModelAndView ResolveGradePage() {
        return new ModelAndView("Grade/grade");
    }
}
