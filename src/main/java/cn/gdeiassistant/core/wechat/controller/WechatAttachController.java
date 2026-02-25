package cn.gdeiassistant.core.wechat.controller;

import cn.gdeiassistant.common.pojo.Entity.User;
import cn.gdeiassistant.common.pojo.Result.JsonResult;
import cn.gdeiassistant.core.userLogin.service.UserLoginService;
import cn.gdeiassistant.core.wechat.service.WechatUserDataService;
import cn.gdeiassistant.integration.httpclient.HttpClientUtils;
import cn.gdeiassistant.common.tools.Utils.StringUtils;
import cn.gdeiassistant.common.validgroup.User.UserLoginValidGroup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;

@RestController
public class WechatAttachController {

    @Autowired
    private UserLoginService userLoginService;

    @Autowired
    private WechatUserDataService wechatUserDataService;

    @RequestMapping(value = "/wechat/userattach", method = RequestMethod.POST)
    public JsonResult WechatUserAttach(HttpServletRequest request, @Validated(value = UserLoginValidGroup.class) User user) throws Exception {
        JsonResult result = new JsonResult();
        String wechatUserID = (String) request.getAttribute("wechatUserID");
        if (StringUtils.isBlank(wechatUserID)) {
            result.setSuccess(false);
            result.setMessage("用户授权已过期，请重新登录并授权");
            return result;
        }
        String sessionId = java.util.UUID.randomUUID().toString().replace("-", "");
        //清除可能存在的旧凭证
        HttpClientUtils.clearHttpClientCookieStore(sessionId);
        //用户登录
        userLoginService.userLogin(sessionId, user.getUsername(), user.getPassword());
        //同步微信用户数据
        wechatUserDataService.syncWechatUserData(user.getUsername(), wechatUserID);
        return new JsonResult(true);
    }
}
