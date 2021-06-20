package edu.gdei.gdeiassistant.Controller.SchoolNews.Controller;

import edu.gdei.gdeiassistant.Exception.DatabaseException.DataNotExistException;
import edu.gdei.gdeiassistant.Pojo.Entity.NewInfo;
import edu.gdei.gdeiassistant.Service.SchoolNews.SchoolNewsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class SchoolNewsController {

    @Autowired
    private SchoolNewsService schoolNewsService;

    @RequestMapping(value = "/news", method = RequestMethod.GET)
    public ModelAndView ResolveNewsPage() {
        return new ModelAndView("News/news");
    }

    @RequestMapping(value = "/new/id/{id}", method = RequestMethod.GET)
    public ModelAndView ShowNewDetailInfo(@PathVariable("id") String id) throws DataNotExistException {
        ModelAndView modelAndView = new ModelAndView("News/newDetail");
        NewInfo newInfo = schoolNewsService.QueryNewDetailInfo(id);
        modelAndView.addObject("NewDetail", newInfo);
        return modelAndView;
    }
}
