package com.linguancheng.gdeiassistant.Controller.GradeQuery;

import com.linguancheng.gdeiassistant.Service.GradeQuery.GradeQueryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

/**
 * Created by linguancheng on 2017/7/22.
 */

@Controller
public class GradeQueryController {

    @Autowired
    private GradeQueryService gradeQueryService;

    @RequestMapping(value = "/grade")
    public ModelAndView ResolveGradePage() {
        return new ModelAndView("Grade/grade");
    }
}
