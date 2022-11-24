package cn.gdeiassistant.Controller.IPAddress.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;

@Controller
public class IPAddressController {

    /**
     * 进行最近登录记录页面
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/ip", method = RequestMethod.GET)
    public ModelAndView ResolveIPAddressPage(HttpServletRequest request) {
        return new ModelAndView("IPAddress/ipaddress");
    }
}
