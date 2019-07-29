package edu.gdei.gdeiassistant.Controller.Theme;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class ThemeController {

    /**
     * 进入主题设置页面
     *
     * @return
     */
    @RequestMapping(value = "/theme", method = RequestMethod.GET)
    public ModelAndView ResolveThemeSettingPage() {
        return new ModelAndView("Theme/theme");
    }
}
