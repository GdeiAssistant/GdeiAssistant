package cn.gdeiassistant.Controller.Email.Controller;

import cn.gdeiassistant.Pojo.Entity.Email;
import cn.gdeiassistant.Service.AccountManagement.Email.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;

@Controller
public class EmailController {

    @Autowired
    public EmailService emailService;

    /**
     * 进入绑定邮箱页面
     *
     * @return
     */
    @RequestMapping(value = "/email", method = RequestMethod.GET)
    public ModelAndView ResolvePhonePage(HttpServletRequest request)  {
        ModelAndView modelAndView = new ModelAndView("Email/index");
        Email email = emailService.QueryUserEmail(request.getSession().getId());
        modelAndView.addObject("Email", email);
        return modelAndView;
    }

    /**
     * 进入修改绑定手机页面
     *
     * @return
     */
    @RequestMapping(value = "/email/change", method = RequestMethod.GET)
    public ModelAndView ResolveEmailChangePage() {
        return new ModelAndView("Email/change");
    }

}
