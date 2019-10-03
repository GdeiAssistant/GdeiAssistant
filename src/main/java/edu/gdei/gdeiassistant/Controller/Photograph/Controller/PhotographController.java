package edu.gdei.gdeiassistant.Controller.Photograph.Controller;

import edu.gdei.gdeiassistant.Annotation.CheckAuthentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class PhotographController {

    /**
     * 进入拍好校园主页/最美生活照页面
     *
     * @return
     */
    @RequestMapping(value = "/photograph", method = RequestMethod.GET)
    @CheckAuthentication(name = "photograph")
    public ModelAndView ResolveLifePhotographPage() {
        return new ModelAndView("Photograph/index");
    }

    /**
     * 进入拍好校园晒照上传页面
     *
     * @return
     */
    @RequestMapping(value = "/photograph/upload", method = RequestMethod.GET)
    @CheckAuthentication(name = "photograph")
    public ModelAndView ResolvePhotographPage() {
        return new ModelAndView("Photograph/upload");
    }
}
