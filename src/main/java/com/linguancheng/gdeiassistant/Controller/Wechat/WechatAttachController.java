package com.linguancheng.gdeiassistant.Controller.Wechat;

import com.linguancheng.gdeiassistant.Pojo.Entity.User;
import com.linguancheng.gdeiassistant.Pojo.Result.JsonResult;
import com.linguancheng.gdeiassistant.Pojo.UserLogin.UserCertificate;
import com.linguancheng.gdeiassistant.Service.UserData.UserDataService;
import com.linguancheng.gdeiassistant.Service.UserLogin.UserLoginService;
import com.linguancheng.gdeiassistant.Service.Wechat.WechatService;
import com.linguancheng.gdeiassistant.Service.Wechat.WechatUserDataService;
import com.linguancheng.gdeiassistant.Tools.HttpClientUtils;
import com.linguancheng.gdeiassistant.Tools.StringUtils;
import com.linguancheng.gdeiassistant.ValidGroup.User.UserLoginValidGroup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;

@Controller
public class WechatAttachController {

    private String appid;

    @Autowired
    private UserLoginService userLoginService;

    @Autowired
    private UserDataService userDataService;

    @Autowired
    private WechatService wechatService;

    @Autowired
    private WechatUserDataService wechatUserDataService;

    @Value("#{propertiesReader['wechat.account.appid']}")
    public void setAppid(String appid) {
        this.appid = appid;
    }

    /**
     * 进入绑定微信页面
     *
     * @param request
     * @param code
     * @param state
     * @return
     * @throws UnsupportedEncodingException
     */
    @RequestMapping(value = "/wechat/attach", method = RequestMethod.GET)
    public ModelAndView ResolveWechatUserAttachPage(HttpServletRequest request, String code, String state) throws UnsupportedEncodingException {
        ModelAndView modelAndView = new ModelAndView();
        if (code == null) {
            //未得到OAuth2.0授权
            if (state == null) {
                //用户未登录授权
                String uuid = StringUtils.randomUUID();
                request.getSession().setAttribute("wechatUUID", uuid);
                modelAndView.setViewName("redirect:https://open.weixin.qq.com/connect/oauth2/authorize?appid="
                        + appid + "&redirect_uri=" + URLEncoder.encode("https://www.gdeiassistant.cn/wechat/attach"
                        , "UTF-8") + "&response_type=code&scope=snsapi_userinfo&state=" + uuid + "#wechat_redirect");
                return modelAndView;
            }
            //用户拒绝授权
            modelAndView.setViewName("Error/commonError");
            modelAndView.addObject("ErrorTitle", "微信授权失败");
            modelAndView.addObject("ErrorMessage", "用户拒绝了微信登录授权");
            return modelAndView;
        }
        //得到OAuth2.0授权
        if (state != null && state.equals(request.getSession().getAttribute("wechatUUID"))) {
            //微信登录授权成功
            Map<String, String> authorizationMap = wechatService.GetAccessTokenAndOpenId(code);
            if (authorizationMap != null) {
                String access_token = authorizationMap.get("access_token");
                String openid = authorizationMap.get("openid");
                String unionid = wechatService.GetUserUnionID(access_token, openid);
                if (StringUtils.isNotBlank(unionid)) {
                    //获取UnionID成功
                    request.getSession().setAttribute("wechatUserID", unionid);
                    modelAndView.setViewName("Wechat/wechatAttach");
                    return modelAndView;
                }
                //获取UnionID失败
                modelAndView.setViewName("Error/commonError");
                modelAndView.addObject("ErrorTitle", "微信授权失败");
                modelAndView.addObject("ErrorMessage", "获取用户统一标识失败");
                return modelAndView;
            }
            //服务器异常
            modelAndView.setViewName("Error/commonError");
            modelAndView.addObject("ErrorTitle", "微信授权失败");
            modelAndView.addObject("ErrorMessage", "获取访问权限令牌失败");
            return modelAndView;
        }
        //请求回调状态码不匹配
        modelAndView.setViewName("Error/commonError");
        modelAndView.addObject("ErrorTitle", "微信授权失败");
        modelAndView.addObject("ErrorMessage", "请求回调状态码不匹配");
        return modelAndView;
    }

    @RequestMapping(value = "/wechat/userattach", method = RequestMethod.POST)
    @ResponseBody
    public JsonResult WechatUserAttach(HttpServletRequest request, HttpServletResponse response
            , @Validated(value = UserLoginValidGroup.class) User user
            , boolean relink, BindingResult bindingResult) throws Exception {
        JsonResult result = new JsonResult();
        if (bindingResult.hasErrors()) {
            result.setSuccess(false);
            result.setMessage("请求参数异常");
            return result;
        }
        String wechatUserID = (String) request.getSession().getAttribute("wechatUserID");
        if (StringUtils.isBlank(wechatUserID)) {
            result.setSuccess(false);
            result.setMessage("用户授权已过期，请重新登录并授权");
            return result;
        }
        //清除已登录用户的用户凭证记录
        HttpClientUtils.ClearHttpClientCookieStore(request.getSession().getId());
        UserCertificate userCertificate = userLoginService.UserLogin(request.getSession().getId(), user, true);
        //同步用户教务系统账号信息到数据库
        userDataService.SyncUserData(userCertificate.getUser());
        //同步微信数据
        wechatUserDataService.SyncWechatUserData(userCertificate.getUser().getUsername(), wechatUserID);
        //将用户信息数据写入Session
        request.getSession().setAttribute("username", userCertificate.getUser().getUsername());
        request.getSession().setAttribute("password", userCertificate.getUser().getPassword());
        //异步同步教务系统会话
        userLoginService.AsyncUpdateSession(request);
        result.setSuccess(true);
        return result;
    }
}
