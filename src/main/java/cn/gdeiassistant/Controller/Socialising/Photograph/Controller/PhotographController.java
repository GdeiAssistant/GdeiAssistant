package cn.gdeiassistant.Controller.Socialising.Photograph.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;

@Controller
public class PhotographController {

    /**
     * 进入拍好校园主页/最美生活照页面
     *
     * @return
     */
    @RequestMapping(value = "/photograph", method = RequestMethod.GET)
    public ModelAndView ResolveLifePhotographPage(HttpServletRequest request) {
        return new ModelAndView("Photograph/index");
    }

    /**
     * 进入拍好校园晒照上传页面
     *
     * @return
     */
    @RequestMapping(value = "/photograph/upload", method = RequestMethod.GET)
    public ModelAndView ResolvePhotographPage(HttpServletRequest request) {
        return new ModelAndView("Photograph/upload");
    }
}
