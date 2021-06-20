package cn.gdeiassistant.Controller.Privacy.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class PrivacyController {

    /**
     * 进入隐私设置界面
     *
     * @return
     */
    @RequestMapping(value = "/privacy", method = RequestMethod.GET)
    public ModelAndView ResolvePrivacySettingPage() {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("Profile/privacy");
        return modelAndView;
    }
}
