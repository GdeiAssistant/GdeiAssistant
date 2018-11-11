package com.linguancheng.gdeiassistant.Controller.YiBan;

import com.linguancheng.gdeiassistant.Enum.Base.LoginResultEnum;
import com.linguancheng.gdeiassistant.Exception.CommonException.TransactionException;
import com.linguancheng.gdeiassistant.Pojo.Entity.User;
import com.linguancheng.gdeiassistant.Pojo.Redirect.RedirectInfo;
import com.linguancheng.gdeiassistant.Pojo.Result.JsonResult;
import com.linguancheng.gdeiassistant.Pojo.Result.BaseResult;
import com.linguancheng.gdeiassistant.Pojo.UserLogin.UserCertificate;
import com.linguancheng.gdeiassistant.Service.UserData.UserDataService;
import com.linguancheng.gdeiassistant.Service.UserLogin.UserLoginService;
import com.linguancheng.gdeiassistant.Service.YiBan.YiBanUserDataService;
import com.linguancheng.gdeiassistant.ValidGroup.User.UserLoginValidGroup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

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
    public JsonResult YiBanUserAttach(HttpServletRequest request, HttpServletResponse response
            , @Validated(value = UserLoginValidGroup.class) User user
            , boolean relink, BindingResult bindingResult) throws ServletException, IOException {
        JsonResult result = new JsonResult();
        if (bindingResult.hasErrors()) {
            result.setSuccess(false);
            result.setMessage("请求参数异常");
            return result;
        }
        String yiBanUserID = (String) request.getSession().getAttribute("yiBanUserID");
        if (yiBanUserID == null || yiBanUserID.trim().isEmpty()) {
            result.setSuccess(false);
            result.setMessage("用户授权已过期，请重新登录并授权");
            return result;
        }
        //清除已登录用户的用户凭证记录
        userLoginService.ClearUserLoginCredentials(request);
        BaseResult<UserCertificate, LoginResultEnum> userLoginResult = userLoginService.UserLogin(request, user, true);
        switch (userLoginResult.getResultType()) {
            case PASSWORD_ERROR:
                result.setSuccess(false);
                result.setMessage("用户账户或密码错误，请检查并重试");
                break;

            case TIME_OUT:
                if (!relink) {
                    //如果第一次连接失败,则重新尝试一次
                    request.getRequestDispatcher("/yiban/userattach?relink=true").forward(request, response);
                } else {
                    result.setSuccess(false);
                    result.setMessage("网络连接超时，请重试");
                }
                break;

            case SERVER_ERROR:
                result.setSuccess(false);
                result.setMessage("教务系统维护中，请稍候再试");
                break;

            case LOGIN_SUCCESS:
                //同步用户教务系统账号信息到数据库
                User resultUser = userLoginResult.getResultData().getUser();
                //同步用户数据
                try {
                    userDataService.SyncUserData(resultUser);
                    //同步易班数据
                    if (yiBanUserDataService.SyncYiBanUserData(resultUser.getUsername(), yiBanUserID)) {
                        //将用户信息数据写入Session
                        request.getSession().setAttribute("username", resultUser.getUsername());
                        request.getSession().setAttribute("password", resultUser.getPassword());
                        userLoginService.AsyncUpdateSession(request);
                        result.setSuccess(true);
                        return result;
                    }
                } catch (TransactionException e) {
                    result.setSuccess(false);
                    result.setMessage("学院系统维护中，请稍候再试");
                }
                break;
        }
        return result;
    }
}
