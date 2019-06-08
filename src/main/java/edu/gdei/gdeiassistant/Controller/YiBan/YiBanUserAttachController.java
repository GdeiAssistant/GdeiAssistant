package edu.gdei.gdeiassistant.Controller.YiBan;

import edu.gdei.gdeiassistant.Enum.UserGroup.UserGroupEnum;
import edu.gdei.gdeiassistant.Pojo.Entity.User;
import edu.gdei.gdeiassistant.Pojo.Redirect.RedirectInfo;
import edu.gdei.gdeiassistant.Pojo.Result.JsonResult;
import edu.gdei.gdeiassistant.Pojo.UserLogin.UserCertificate;
import edu.gdei.gdeiassistant.Service.UserData.UserDataService;
import edu.gdei.gdeiassistant.Service.UserLogin.UserLoginService;
import edu.gdei.gdeiassistant.Service.YiBan.YiBanUserDataService;
import edu.gdei.gdeiassistant.Tools.HttpClientUtils;
import edu.gdei.gdeiassistant.Tools.StringUtils;
import edu.gdei.gdeiassistant.ValidGroup.User.UserLoginValidGroup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;

@Controller
public class YiBanUserAttachController {

    @Autowired
    private UserLoginService userLoginService;

    @Autowired
    private UserDataService userDataService;

    @Autowired
    private YiBanUserDataService yiBanUserDataService;

    @RequestMapping("/yiban/attach")
    public ModelAndView ResolveYiBanUserAttachPage(HttpServletRequest request, RedirectInfo redirectInfo) {
        ModelAndView modelAndView = new ModelAndView();
        String userid = (String) request.getSession().getAttribute("yiBanUserID");
        if (userid == null || userid.trim().isEmpty()) {
            modelAndView.addObject("ErrorMessage", "用户授权已过期，请重新登录并授权");
            modelAndView.setViewName("YiBan/yibanError");
            return modelAndView;
        }
        if (redirectInfo.needToRedirect()) {
            modelAndView.addObject("RedirectURL", redirectInfo.getRedirect_url());
        }
        modelAndView.setViewName("YiBan/yibanAttach");
        return modelAndView;
    }

    @RequestMapping("/yiban/userattach")
    @ResponseBody
    public JsonResult YiBanUserAttach(HttpServletRequest request, @Validated(value = UserLoginValidGroup.class) User user) throws Exception {
        JsonResult result = new JsonResult();
        String yiBanUserID = (String) request.getSession().getAttribute("yiBanUserID");
        if (StringUtils.isBlank(yiBanUserID)) {
            return new JsonResult(false, "用户授权已过期，请重新登录并授权");
        }
        //清除已登录用户的用户凭证记录
        HttpClientUtils.ClearHttpClientCookieStore(request.getSession().getId());
        UserCertificate userCertificate = userLoginService
                .UserLogin(request.getSession().getId(), user, true);
        //同步用户数据
        userDataService.SyncUserData(userCertificate.getUser());
        //同步易班数据
        yiBanUserDataService.SyncYiBanUserData(userCertificate.getUser().getUsername(), yiBanUserID);
        //将用户信息数据写入Session
        request.getSession().setAttribute("username", userCertificate.getUser().getUsername());
        request.getSession().setAttribute("password", userCertificate.getUser().getPassword());
        request.getSession().setAttribute("group", userCertificate.getUser().getGroup());
        if (userCertificate.getUser().getGroup().equals(UserGroupEnum.STUDENT.getValue())
                || userCertificate.getUser().getGroup().equals(UserGroupEnum.TEST.getValue())) {
            //若当前用户组为学生用户或测试用户，则异步地与教务系统会话进行同步
            userLoginService.AsyncUpdateSession(request);
        }
        result.setSuccess(true);
        return result;
    }
}
