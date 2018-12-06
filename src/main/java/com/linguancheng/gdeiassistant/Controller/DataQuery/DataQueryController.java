package com.linguancheng.gdeiassistant.Controller.DataQuery;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class DataQueryController {

    @RequestMapping("/data")
    public ModelAndView ResolveDataQueryPage() {
        return new ModelAndView("Data/index");
    }

    @RequestMapping("/data/fees/electricity")
    public ModelAndView ResolveElectricityFeesData() {
        return new ModelAndView("Data/electricityFees");
    }
}
