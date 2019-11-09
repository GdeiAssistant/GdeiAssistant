package edu.gdei.gdeiassistant.Controller.Wechat.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class WechatAccountController {

    /**
     * 进入校园公众号列表页面
     *
     * @return
     */
    @RequestMapping(value = "/wechataccount", method = RequestMethod.GET)
    public ModelAndView ResolveWechatAccountPage() {
        return new ModelAndView("Wechat/wechatAccount");
    }
}
