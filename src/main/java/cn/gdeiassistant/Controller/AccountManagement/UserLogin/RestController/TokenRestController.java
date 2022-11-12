package cn.gdeiassistant.Controller.AccountManagement.UserLogin.RestController;

import cn.gdeiassistant.Annotation.DeviceUpdateRequirement;
import cn.gdeiassistant.Annotation.RecordIPAddress;
import cn.gdeiassistant.Enum.IPAddress.IPAddressEnum;
import cn.gdeiassistant.Pojo.Entity.Device;
import cn.gdeiassistant.Pojo.Entity.Token;
import cn.gdeiassistant.Pojo.Result.JsonResult;
import cn.gdeiassistant.Pojo.TokenRefresh.TokenRefreshJsonResult;
import cn.gdeiassistant.Pojo.TokenRefresh.TokenRefreshResult;
import cn.gdeiassistant.Service.AccountManagement.Token.LoginTokenService;
import cn.gdeiassistant.ValidGroup.Device.DeviceDataValidGroup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
public class TokenRestController {

    @Autowired
    private LoginTokenService loginTokenService;

    /**
     * 使令牌信息主动过期
     *
     * @param request
     * @param accessTokenSignature
     * @return
     */
    @RequestMapping(value = "/rest/token/expire", method = RequestMethod.POST)
    public JsonResult ExpireToken(HttpServletRequest request
            , @RequestParam("token") String accessTokenSignature) throws Exception {
        loginTokenService.ExpireToken(accessTokenSignature);
        return new JsonResult(true);
    }

    /**
     * 刷新登录令牌
     *
     * @param request
     * @param refreshTokenSignature
     * @return
     */
    @RequestMapping(value = "/rest/token/refresh", method = RequestMethod.POST)
    @DeviceUpdateRequirement
    @RecordIPAddress(type = IPAddressEnum.LOGIN, rest = true)
    public TokenRefreshJsonResult RefreshToken(HttpServletRequest request
            , @RequestParam("token") String refreshTokenSignature
            , @Validated(value = DeviceDataValidGroup.class) Device device) throws Exception {
        TokenRefreshJsonResult result = new TokenRefreshJsonResult();
        //刷新令牌
        TokenRefreshResult tokenRefreshResult = loginTokenService.RefreshToken(request.getSession().getId()
                , refreshTokenSignature);
        result.setAccessToken(new Token(tokenRefreshResult.getAccessToken()));
        result.setRefreshToken(new Token(tokenRefreshResult.getRefreshToken()));
        //将令牌签名写入Request作用域，LoginTokenAspect将保存设备信息到缓存
        request.setAttribute("token", tokenRefreshResult.getAccessToken().getSignature());
        return result;
    }

}
