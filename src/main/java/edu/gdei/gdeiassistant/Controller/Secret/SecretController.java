package edu.gdei.gdeiassistant.Controller.Secret;

import edu.gdei.gdeiassistant.Pojo.Entity.Secret;
import edu.gdei.gdeiassistant.Service.Secret.SecretService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Controller
public class SecretController {

    @Autowired
    private SecretService secretService;

    /**
     * 进入校园树洞应用首页
     *
     * @return
     */
    @RequestMapping(value = {"/secret"}, method = RequestMethod.GET)
    public ModelAndView ResolveSecretIndexPage() {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("Secret/secretIndex");
        return modelAndView;
    }

    /**
     * 进入树洞信息发布界面
     *
     * @return
     */
    @RequestMapping(value = {"/secret/publish"}, method = RequestMethod.GET)
    public ModelAndView ResolveSecretPublishPage() {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("Secret/secretPublish");
        return modelAndView;
    }

    /**
     * 进入树洞个人页面
     *
     * @param request
     * @return
     */
    @RequestMapping(value = {"/secret/profile"}, method = RequestMethod.GET)
    public ModelAndView ResolveSecretProfilePage(HttpServletRequest request) throws Exception {
        ModelAndView modelAndView = new ModelAndView();
        String username = (String) request.getSession().getAttribute("username");
        List<Secret> secretList = secretService.GetSecretInfo(username);
        modelAndView.addObject("SecretList", secretList);
        modelAndView.setViewName("Secret/secretProfile");
        return modelAndView;
    }

    /**
     * 进入树洞信息详细信息页面
     *
     * @param request
     * @return
     */
    @RequestMapping(value = {"/secret/detail/id/{id}"}, method = RequestMethod.GET)
    public ModelAndView ResolveSecretDetailPage(HttpServletRequest request, @PathVariable("id") int id) throws Exception {
        ModelAndView modelAndView = new ModelAndView();
        String username = (String) request.getSession().getAttribute("username");
        Secret secret = secretService.GetSecretDetailInfo(id, username);
        modelAndView.setViewName("Secret/secretDetail");
        modelAndView.addObject("Secret", secret);
        return modelAndView;
    }
}
