package edu.gdei.gdeiassistant.Controller.YiBan;

import cn.yiban.open.Authorize;
import cn.yiban.open.FrameUtil;
import com.google.gson.Gson;
import edu.gdei.gdeiassistant.Pojo.Entity.User;
import edu.gdei.gdeiassistant.Pojo.Entity.YiBanAuthorizeInfo;
import edu.gdei.gdeiassistant.Pojo.Entity.YiBanUser;
import edu.gdei.gdeiassistant.Pojo.Redirect.RedirectInfo;
import edu.gdei.gdeiassistant.Pojo.YiBan.YiBanTokenJsonResult;
import edu.gdei.gdeiassistant.Service.UserData.UserDataService;
import edu.gdei.gdeiassistant.Service.UserLogin.UserLoginService;
import edu.gdei.gdeiassistant.Service.YiBan.YiBanAPIService;
import edu.gdei.gdeiassistant.Service.YiBan.YiBanUserDataService;
import edu.gdei.gdeiassistant.Tools.HttpClientUtils;
import edu.gdei.gdeiassistant.Tools.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

@Controller
public class YiBanLoginController {

    @Autowired
    private YiBanAPIService yiBanAPIService;

    @Autowired
    private UserLoginService userLoginService;

    @Autowired
    private UserDataService userDataService;

    @Autowired
    private YiBanUserDataService yiBanUserDataService;

    @Resource(name = "yiBanAuthorizeMap")
    private Map<String, YiBanAuthorizeInfo> yiBanAuthorizeMap;

    @Resource(name = "yiBanAppAuthorizeMap")
    private Map<String, YiBanAuthorizeInfo> yiBanAppAuthorizeMap;

    @Resource(name = "yiBanLightAppAuthorizeMap")
    private Map<String, YiBanAuthorizeInfo> yiBanLightAppAuthorizeMap;

    /**
     * 通过易班帐号绑定的教务系统帐号登录系统
     *
     * @param request
     * @return
     */
    @RequestMapping("/yiban/userlogin")
    public ModelAndView YiBanUserLogin(HttpServletRequest request, RedirectInfo redirectInfo) throws Exception {
        ModelAndView modelAndView = new ModelAndView();
        String username = (String) request.getSession().getAttribute("username");
        if (username == null || username.trim().isEmpty()) {
            modelAndView.addObject("ErrorMessage", "系统维护中，请稍候再试");
            modelAndView.setViewName("YiBan/yibanError");
            return modelAndView;
        }
        //清除已登录用户的用户凭证记录
        HttpClientUtils.ClearHttpClientCookieStore(request.getSession().getId());
        User user = yiBanAPIService.YiBanQuickLogin(request.getSession().getId(), username);
        //同步数据库用户数据
        userDataService.SyncUserData(user);
        //将用户信息数据写入Session
        request.getSession().setAttribute("username", user.getUsername());
        request.getSession().setAttribute("group", user.getGroup());
        userLoginService.AsyncUpdateSession(request);
        if (redirectInfo.needToRedirect()) {
            modelAndView.setViewName("redirect:/" + redirectInfo.getRedirect_url());
        } else {
            modelAndView.setViewName("redirect:/index");
        }
        return modelAndView;
    }

    /**
     * 获取易班用户授权，并返回回调地址
     *
     * @param request
     * @return
     */
    @RequestMapping("/yiban/authorize")
    public ModelAndView AuthorizeYiBan(HttpServletRequest request) {
        ModelAndView modelAndView = new ModelAndView();
        YiBanAuthorizeInfo yiBanAuthorizeInfo = (YiBanAuthorizeInfo) request.getAttribute("YiBanAuthorizeInfo");
        if (yiBanAuthorizeInfo == null) {
            modelAndView.addObject("ErrorMessage", "请求参数不合法");
            modelAndView.setViewName("YiBan/yibanError");
            return modelAndView;
        }
        Authorize authorize = new Authorize(yiBanAuthorizeInfo.getAppID(), yiBanAuthorizeInfo.getAppSecret());
        String url = authorize.forwardurl(yiBanAuthorizeInfo.getCallbackURL(), "GdeiAssistant", Authorize.DISPLAY_TAG_T.WEB);
        modelAndView.setViewName("redirect:" + url);
        return modelAndView;
    }

    /**
     * 易班网站接入授权前端控制器
     *
     * @param request
     * @return
     */
    @RequestMapping({"/yiban/dispatch", "/yiban/dispatch/{app}"})
    public ModelAndView YiBanDispatch(HttpServletRequest request, @PathVariable(value = "app", required = false) String redirect_url) throws Exception {
        ModelAndView modelAndView = new ModelAndView();
        RedirectInfo redirectInfo = new RedirectInfo();
        if (!StringUtils.isBlank(redirect_url)) {
            //获取请求携带的重定向地址
            redirectInfo.setRedirect_url(redirect_url);
        } else {
            //获取易班授权回调的重定向地址
            String yiBanCallbackRedirectURL = (String) request.getSession().getAttribute("YiBanCallbackRedirectURL");
            if (yiBanCallbackRedirectURL != null && !yiBanCallbackRedirectURL.trim().isEmpty()) {
                redirectInfo.setRedirect_url(yiBanCallbackRedirectURL);
                request.getSession().removeAttribute("YiBanCallbackRedirectURL");
            }
        }
        String token;
        if (request.getSession().getAttribute("yiBanAccessToken") != null
                && !(((String) request.getSession().getAttribute("yiBanAccessToken"))).trim().isEmpty()
                && request.getSession().getAttribute("yiBanAccessTokenExpires") != null
                && (((int) request.getSession().getAttribute("yiBanAccessTokenExpires")) - ((int) (System.currentTimeMillis() / 1000))) >= 120) {
            //直接从Session中获取易班的访问权限Token
            token = (String) request.getSession().getAttribute("yiBanAccessToken");
        } else {
            //获取重定向地址对应的易班应用配置信息
            YiBanAuthorizeInfo yiBanAuthorizeInfo = yiBanAuthorizeMap
                    .getOrDefault(redirectInfo.getRedirect_url()
                            , yiBanAuthorizeMap.get("/yiban/userlogin"));
            String code = request.getParameter("code");
            if (code == null || code.trim().isEmpty()) {
                //如果未进行授权,则调用易班授权
                modelAndView.addObject("YiBanAuthorizeInfo", yiBanAuthorizeInfo);
                if (redirectInfo.needToRedirect()) {
                    request.getSession().setAttribute("YiBanCallbackRedirectURL", redirectInfo.getRedirect_url());
                }
                modelAndView.setViewName("forward:/yiban/authorize");
                return modelAndView;
            }
            //成功获取授权，获取Token
            Authorize authorize = new Authorize(yiBanAuthorizeInfo.getAppID(), yiBanAuthorizeInfo.getAppSecret());
            String queryTokenJsonText = authorize.querytoken(code, yiBanAuthorizeInfo.getCallbackURL());
            YiBanTokenJsonResult yiBanTokenJsonResult = new Gson().fromJson(queryTokenJsonText, YiBanTokenJsonResult.class);
            token = yiBanTokenJsonResult.getAccessToken();
            request.getSession().setAttribute("yiBanAccessToken", token);
            request.getSession().setAttribute("yiBanAccessTokenExpires", Integer.valueOf(yiBanTokenJsonResult.getExpires()));
        }
        //通过凭证令牌登录广东二师助手系统
        YiBanUser yiBanUser = yiBanAPIService.getYiBanUserInfo(token);
        //保存用户UserID到Session中
        request.getSession().setAttribute("yiBanUserID", yiBanUser.getUserID());
        //检查账号绑定情况
        String yiBanAttachUsername = yiBanUserDataService.GetYiBanAttachUsername(yiBanUser.getUserID());
        if (StringUtils.isBlank(yiBanAttachUsername)) {
            //未绑定账号
            if (redirectInfo.needToRedirect()) {
                modelAndView.setViewName("redirect:/yiban/attach?redirect_url=" + redirectInfo.getRedirect_url());
            } else {
                modelAndView.setViewName("redirect:/yiban/attach");
            }
        } else {
            //已绑定账号
            request.getSession().setAttribute("username", yiBanAttachUsername);
            if (redirectInfo.needToRedirect()) {
                //重定向到易班应用活动页面
                modelAndView.setViewName("redirect:/yiban/userlogin?redirect_url=" + redirectInfo.getRedirect_url());
            } else {
                modelAndView.setViewName("redirect:/yiban/userlogin?redirect_url=");
            }
        }
        return modelAndView;
    }

    /**
     * 易班轻应用授权前端控制器
     *
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    @RequestMapping(value = {"/yiban/lightapp/dispatch", "/yiban/lightapp/dispatch/{app}"}, method = RequestMethod.GET)
    public ModelAndView YiBanLightAppDispatch(HttpServletRequest request, HttpServletResponse response
            , @PathVariable(value = "app", required = false) String redirect_url) throws Exception {
        ModelAndView modelAndView = new ModelAndView();
        String verify_request = request.getParameter("verify_request");
        String yb_uid = request.getParameter("yb_uid");
        if (StringUtils.isBlank(verify_request) || StringUtils.isBlank(yb_uid)) {
            modelAndView.addObject("ErrorMessage", "请求参数不合法");
            modelAndView.setViewName("YiBan/yibanError");
            return modelAndView;
        }
        YiBanAuthorizeInfo yiBanAuthorizeInfo = yiBanLightAppAuthorizeMap
                .getOrDefault(redirect_url, yiBanLightAppAuthorizeMap.get("userlogin"));
        FrameUtil frameUtil = new FrameUtil(request, response, yiBanAuthorizeInfo.getAppID()
                , yiBanAuthorizeInfo.getAppSecret(), yiBanAuthorizeInfo.getCallbackURL());
        String yiBanUserID = (String) request.getSession().getAttribute("yiBanUserID");
        String yiBanAccessToken = (String) request.getSession().getAttribute("yiBanAccessToken");
        if (StringUtils.isBlank(yiBanUserID) || StringUtils.isBlank(yiBanAccessToken)) {
            boolean result = frameUtil.perform();
            if (result) {
                yiBanUserID = frameUtil.getUserId();
                yiBanAccessToken = frameUtil.getAccessToken();
                request.getSession().setAttribute("yiBanAccessToken", yiBanAccessToken);
                request.getSession().setAttribute("yiBanUserID", yiBanUserID);
            } else {
                //跳转到易班用户授权页面
                modelAndView.setViewName("YiBan/yibanAuthorize");
                modelAndView.addObject("ClientID", yiBanAuthorizeInfo.getAppID());
                modelAndView.addObject("RedirectURL", yiBanAuthorizeInfo.getCallbackURL());
                return modelAndView;
            }
        }
        //检查账号绑定情况，获取用户名
        String yiBanAttachUsername = yiBanUserDataService.GetYiBanAttachUsername(yiBanUserID);
        if (StringUtils.isBlank(yiBanAttachUsername)) {
            //未绑定账号
            if (!StringUtils.isBlank(redirect_url)) {
                modelAndView.setViewName("redirect:/yiban/attach?redirect_url=" + redirect_url);
            } else {
                modelAndView.setViewName("redirect:/yiban/attach");
            }
        } else {
            //已绑定账号
            request.getSession().setAttribute("username", yiBanAttachUsername);
            if (!StringUtils.isBlank(redirect_url)) {
                //重定向到易班应用活动页面
                modelAndView.setViewName("redirect:/yiban/userlogin?redirect_url=" + redirect_url);
            } else {
                modelAndView.setViewName("redirect:/yiban/userlogin");
            }
        }
        return modelAndView;
    }

    /**
     * 易班站内应用授权前端控制器
     *
     * @param request
     * @param response
     * @return
     */
    @RequestMapping(value = {"/yiban/app/dispatch", "/yiban/app/dispatch/{app}"}, method = RequestMethod.POST)
    public ModelAndView YiBanAppDispatch(HttpServletRequest request, HttpServletResponse response
            , @PathVariable(value = "app", required = false) String redirect_url) throws Exception {
        ModelAndView modelAndView = new ModelAndView();
        String verify_request = request.getParameter("verify_request");
        String verify4j_request = request.getParameter("verify4j_request");
        if (verify_request == null || verify_request.trim().isEmpty()
                || verify4j_request == null || verify4j_request.trim().isEmpty()) {
            modelAndView.addObject("ErrorMessage", "请求参数不合法");
            modelAndView.setViewName("YiBan/yibanError");
            return modelAndView;
        }
        YiBanAuthorizeInfo yiBanAuthorizeInfo = yiBanAppAuthorizeMap
                .getOrDefault(redirect_url, yiBanAppAuthorizeMap.get("/yiban/app/userlogin"));
        FrameUtil frameUtil = new FrameUtil(request, response, yiBanAuthorizeInfo.getAppID()
                , yiBanAuthorizeInfo.getAppSecret(), yiBanAuthorizeInfo.getCallbackURL());
        String yiBanUserID = (String) request.getSession().getAttribute("yiBanUserID");
        if (yiBanUserID == null || yiBanUserID.trim().isEmpty()) {
            boolean result = frameUtil.perform();
            if (result) {
                yiBanUserID = frameUtil.getUserId();
                request.getSession().setAttribute("yiBanUserID", yiBanUserID);
            } else {
                //跳转到易班用户授权页面
                modelAndView.setViewName("YiBan/yibanAuthorize");
                modelAndView.addObject("ClientID", yiBanAuthorizeInfo.getAppID());
                modelAndView.addObject("RedirectURL", yiBanAuthorizeInfo.getCallbackURL());
                return modelAndView;
            }
        }
        //检查账号绑定情况，获取绑定的用户名
        String yiBanAttachUsername = yiBanUserDataService.GetYiBanAttachUsername(yiBanUserID);
        if (StringUtils.isBlank(yiBanAttachUsername)) {
            //账号未绑定
            if (!StringUtils.isBlank(redirect_url)) {
                modelAndView.setViewName("redirect:/yiban/attach?redirect_url=" + redirect_url);
            } else {
                modelAndView.setViewName("redirect:/yiban/attach");
            }
        } else {
            //账号已绑定
            request.getSession().setAttribute("username", yiBanAttachUsername);
            if (!StringUtils.isBlank(redirect_url)) {
                //重定向到易班应用活动页面
                modelAndView.setViewName("redirect:/yiban/userlogin?redirect_url=" + redirect_url);
            } else {
                modelAndView.setViewName("redirect:/yiban/userlogin");
            }
        }
        return modelAndView;
    }
}
