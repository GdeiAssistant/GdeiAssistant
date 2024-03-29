package cn.gdeiassistant.Controller.Announcement.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;

@Controller
public class AnnouncementController {

    /**
     * 进入通知公告发布页面
     *
     * @return
     */
    @RequestMapping(value = "/announcement/publish", method = RequestMethod.GET)
    public ModelAndView ResolveAnnouncementPage(HttpServletRequest request) {
        return new ModelAndView("Announcement/publish");
    }
}
