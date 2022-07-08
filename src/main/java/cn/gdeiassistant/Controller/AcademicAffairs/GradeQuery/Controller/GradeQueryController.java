package cn.gdeiassistant.Controller.AcademicAffairs.GradeQuery.Controller;

import cn.gdeiassistant.Service.AcademicAffairs.GradeQuery.GradeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class GradeQueryController {

    @Autowired
    private GradeService gradeService;

    @RequestMapping(value = "/grade")
    public ModelAndView ResolveGradePage() {
        return new ModelAndView("Grade/grade");
    }
}
