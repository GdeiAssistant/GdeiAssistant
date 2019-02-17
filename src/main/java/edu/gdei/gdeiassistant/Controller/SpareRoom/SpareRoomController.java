package edu.gdei.gdeiassistant.Controller.SpareRoom;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
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
        //获取可用的日期列表
        LocalDate localDate = LocalDate.now();
        List<Date> dateList = new ArrayList<>();
        for (int i = 0; i < 15; i++) {
            if (i == 0) {
                Instant instant = localDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
                Date date = Date.from(instant);
                dateList.add(date);
            } else {
                localDate = localDate.plusDays(1);
                Instant instant = localDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
                Date date = Date.from(instant);
                dateList.add(date);
            }
        }
        modelAndView.addObject("DateList", dateList);
        return modelAndView;
    }
}