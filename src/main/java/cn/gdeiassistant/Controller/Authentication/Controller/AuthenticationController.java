package cn.gdeiassistant.Controller.Authentication.Controller;

import cn.gdeiassistant.Pojo.Entity.Authentication;
import cn.gdeiassistant.Service.AccountManagement.Authentication.AuthenticationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;

@Controller
public class AuthenticationController {

    @Autowired
    private AuthenticationService authenticationService;

    /**
     * 实名认证界面
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/authentication",method = RequestMethod.GET)
    public ModelAndView ResolveAuthenticationPage(HttpServletRequest request){
        ModelAndView modelAndView = new ModelAndView("Authentication/index");
        Authentication authentication = authenticationService.QueryAuthentication(request.getSession().getId());
        if(authentication!=null){
            modelAndView.addObject("Authentication", "true");
        }
        return modelAndView;
    }

    /**
     * 更新实名认证页面
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/authentication/update",method = RequestMethod.GET)
    public ModelAndView ResolveAuthenticationEditPage(HttpServletRequest request){
        return new ModelAndView("Authentication/update");
    }
}
