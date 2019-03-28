package edu.gdei.gdeiassistant.Controller.Reading;

import edu.gdei.gdeiassistant.Exception.CommonException.ResourceNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class ReadingController {

    /**
     * 解析推荐阅读页面
     *
     * @param id
     * @return
     */
    @RequestMapping(value = "/reading/id/{id}", method = RequestMethod.GET)
    public ModelAndView ResolveReadingPage(@PathVariable("id") Integer id) throws ResourceNotFoundException {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("/Reading/" + id);
        if (modelAndView.hasView()) {
            return modelAndView;
        }
        throw new ResourceNotFoundException("没有找到对应的视图资源");
    }
}
