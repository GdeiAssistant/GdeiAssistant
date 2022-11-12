package cn.gdeiassistant.Controller.Phone.Controller;

import cn.gdeiassistant.Pojo.Entity.Phone;
import cn.gdeiassistant.Service.AccountManagement.Phone.PhoneService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;

@Controller
public class PhoneController {

    @Autowired
    public PhoneService phoneService;

    /**
     * 进入绑定手机页面
     *
     * @return
     */
    @RequestMapping(value = "/phone", method = RequestMethod.GET)
    public ModelAndView ResolvePhonePage(HttpServletRequest request)  {
        ModelAndView modelAndView = new ModelAndView("Phone/index");
        Phone phone = phoneService.QueryUserPhone(request.getSession().getId());
        modelAndView.addObject("Phone", phone);
        return modelAndView;
    }

    /**
     * 进入修改绑定手机页面
     *
     * @return
     */
    @RequestMapping(value = "/phone/change", method = RequestMethod.GET)
    public ModelAndView ResolvePhoneChangePage() {
        return new ModelAndView("Phone/change");
    }
}
