package edu.gdei.gdeiassistant.Controller.SpareRoom;

import edu.gdei.gdeiassistant.Tools.WeekUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.ArrayList;
import java.util.List;

@Controller
public class SpareRoomController {

    /**
     * 进入空课表查询页面
     *
     * @return
     */
    @RequestMapping(value = "/spare")
    public ModelAndView ResolveSparePage() {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("Spare/spare");
        //获取可用的周数列表
        List<Integer> weekList = new ArrayList<>();
        for (int i = WeekUtils.GetCurrentWeek(); i <= 20; i++) {
            weekList.add(i);
        }
        modelAndView.addObject("WeekList", weekList);
        return modelAndView;
    }
}