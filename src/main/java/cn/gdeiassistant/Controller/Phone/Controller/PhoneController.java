package cn.gdeiassistant.Controller.Phone.Controller;

import com.taobao.wsgsvr.WsgException;
import cn.gdeiassistant.Pojo.Entity.Phone;
import cn.gdeiassistant.Service.Phone.PhoneService;
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
    public ModelAndView ResolvePhonePage(HttpServletRequest request) throws WsgException {
        ModelAndView modelAndView = new ModelAndView("Phone/index");
        String username = (String) request.getSession().getAttribute("username");
        Phone phone = phoneService.QueryUserPhone(username);
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
