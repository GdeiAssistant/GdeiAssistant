package cn.gdeiassistant.core.userLogin.controller;

import cn.gdeiassistant.common.annotation.DeviceUpdateRequirement;
import cn.gdeiassistant.common.annotation.RecordIPAddress;
import cn.gdeiassistant.common.enums.IPAddress.IPAddressEnum;
import cn.gdeiassistant.common.pojo.Entity.Device;
import cn.gdeiassistant.common.pojo.Entity.Token;
import cn.gdeiassistant.common.pojo.Result.JsonResult;
import cn.gdeiassistant.core.tokenRefresh.pojo.TokenRefreshJsonResult;
import cn.gdeiassistant.core.tokenRefresh.pojo.TokenRefreshResult;
import cn.gdeiassistant.core.token.service.LoginTokenService;
import cn.gdeiassistant.common.validgroup.Device.DeviceDataValidGroup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import java.util.UUID;

@RestController
public class TokenController {

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
    public JsonResult expireToken(HttpServletRequest request
            , @RequestParam("token") String accessTokenSignature) throws Exception {
        loginTokenService.expireToken(accessTokenSignature);
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
    public TokenRefreshJsonResult refreshToken(HttpServletRequest request
            , @RequestParam("token") String refreshTokenSignature
            , @Validated(value = DeviceDataValidGroup.class) Device device) throws Exception {
        TokenRefreshJsonResult result = new TokenRefreshJsonResult();
        String sessionId = (String) request.getAttribute("sessionId");
        if (sessionId == null || sessionId.isEmpty()) {
            sessionId = UUID.randomUUID().toString().replace("-", "");
        }
        //刷新令牌
        TokenRefreshResult tokenRefreshResult = loginTokenService.refreshToken(sessionId, refreshTokenSignature);
        result.setAccessToken(new Token(tokenRefreshResult.getAccessToken()));
        result.setRefreshToken(new Token(tokenRefreshResult.getRefreshToken()));
        //将令牌签名写入Request作用域，LoginTokenAspect将保存设备信息到缓存
        request.setAttribute("token", tokenRefreshResult.getAccessToken().getSignature());
        return result;
    }

}
