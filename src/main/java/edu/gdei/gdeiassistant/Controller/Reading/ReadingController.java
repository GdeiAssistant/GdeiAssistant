package edu.gdei.gdeiassistant.Controller.Reading;

import edu.gdei.gdeiassistant.Pojo.Entity.Reading;
import edu.gdei.gdeiassistant.Service.Reading.ReadingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

@Controller
public class ReadingController {

    @Autowired
    private ReadingService readingService;

    /**
     * 进入专题阅读信息列表页面
     *
     * @return
     */
    @RequestMapping(value = "/reading", method = RequestMethod.GET)
    public ModelAndView ResolveReadingPage() {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("Reading/reading");
        List<Reading> readingList = readingService.LoadingAllReadingList();
        modelAndView.addObject("ReadingList", readingList);
        return modelAndView;
    }
}
