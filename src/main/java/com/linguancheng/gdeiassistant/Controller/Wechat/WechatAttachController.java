package com.linguancheng.gdeiassistant.Controller.Wechat;

import com.linguancheng.gdeiassistant.Service.Wechat.WechatService;
import com.linguancheng.gdeiassistant.Tools.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Controller
public class WechatAttachController {

    private String appid;

    @Autowired
    private WechatService wechatService;

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
                        , StandardCharsets.UTF_8) + "&response_type=code&scope=snsapi_userinfo&state=" + uuid + "#wechat_redirect");
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
}
