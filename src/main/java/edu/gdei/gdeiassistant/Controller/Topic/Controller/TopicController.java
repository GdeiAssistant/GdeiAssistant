package edu.gdei.gdeiassistant.Controller.Topic.Controller;

import edu.gdei.gdeiassistant.Annotation.CheckAuthentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;

@Controller
public class TopicController {

    /**
     * 进入话题主页
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/topic", method = RequestMethod.GET)
    @CheckAuthentication(name = "topic")
    public ModelAndView ResolveTopicPage(HttpServletRequest request) {
        return new ModelAndView("Topic/index");
    }

    /**
     * 进入话题发布页
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/topic/publish", method = RequestMethod.GET)
    @CheckAuthentication(name = "topic")
    public ModelAndView ResolveTopicPublishPage(HttpServletRequest request) {
        return new ModelAndView("Topic/publish");
    }

    /**
     * 进入话题搜索页
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/topic/search", method = RequestMethod.GET)
    @CheckAuthentication(name = "topic")
    public ModelAndView ResolveTopicSearchPage(HttpServletRequest request) {
        return new ModelAndView("Topic/search");
    }
}
