package cn.gdeiassistant.Controller.Deletion.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class AccountDeletionController {

    /**
     * 进入账号删除页面
     *
     * @return
     */
    @RequestMapping(value = "/close", method = RequestMethod.GET)
    public ModelAndView ResolveClosePage() {
        return new ModelAndView("Account/closeAccount");
    }
}
