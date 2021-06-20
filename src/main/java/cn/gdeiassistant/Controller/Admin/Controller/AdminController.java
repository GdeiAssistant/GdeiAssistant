package cn.gdeiassistant.Controller.Admin.Controller;

import cn.gdeiassistant.Annotation.UserGroupAccess;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;

@Controller
public class AdminController {

    /**
     * 进入管理员选项主页
     *
     * @return
     */
    @RequestMapping(value = "/admin", method = RequestMethod.GET)
    @UserGroupAccess(group = {1}, rest = false)
    public ModelAndView ResolveAdminPage(HttpServletRequest request) {
        return new ModelAndView("Admin/index");
    }
}
