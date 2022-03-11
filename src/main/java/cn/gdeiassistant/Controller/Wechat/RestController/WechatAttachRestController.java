package cn.gdeiassistant.Controller.Wechat.RestController;

import cn.gdeiassistant.Pojo.Entity.User;
import cn.gdeiassistant.Pojo.Result.JsonResult;
import cn.gdeiassistant.Service.UserLogin.UserLoginService;
import cn.gdeiassistant.Service.Wechat.WechatUserDataService;
import cn.gdeiassistant.Tools.Utils.HttpClientUtils;
import cn.gdeiassistant.Tools.Utils.StringUtils;
import cn.gdeiassistant.ValidGroup.User.UserLoginValidGroup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
public class WechatAttachRestController {

    @Autowired
    private UserLoginService userLoginService;

    @Autowired
    private WechatUserDataService wechatUserDataService;

    @RequestMapping(value = "/wechat/userattach", method = RequestMethod.POST)
    public JsonResult WechatUserAttach(HttpServletRequest request, @Validated(value = UserLoginValidGroup.class) User user) throws Exception {
        JsonResult result = new JsonResult();
        String wechatUserID = (String) request.getSession().getAttribute("wechatUserID");
        if (StringUtils.isBlank(wechatUserID)) {
            result.setSuccess(false);
            result.setMessage("用户授权已过期，请重新登录并授权");
            return result;
        }
        //清除已登录用户的用户凭证记录
        HttpClientUtils.ClearHttpClientCookieStore(request.getSession().getId());
        //用户登录
        userLoginService.UserLogin(request.getSession().getId(), user.getUsername(), user.getPassword());
        //同步微信用户数据
        wechatUserDataService.SyncWechatUserData(user.getUsername(), wechatUserID);
        return new JsonResult(true);
    }
}
