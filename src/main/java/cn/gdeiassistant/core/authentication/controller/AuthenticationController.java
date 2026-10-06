package cn.gdeiassistant.core.authentication.controller;

import cn.gdeiassistant.common.exception.authenticationexception.*;
import cn.gdeiassistant.common.pojo.entity.Authentication;
import cn.gdeiassistant.common.pojo.result.DataJsonResult;
import cn.gdeiassistant.common.pojo.result.JsonResult;
import cn.gdeiassistant.core.authentication.service.AuthenticationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletRequest;

@RestController
public class AuthenticationController {

    @Autowired
    private AuthenticationService authenticationService;

    /**
     * 查询实名认证状态
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/api/authentication/status", method = RequestMethod.GET)
    public DataJsonResult<Boolean> getAuthenticationStatus(HttpServletRequest request) {
        String sessionId = (String) request.getAttribute("sessionId");
        Authentication authentication = authenticationService.queryAuthentication(sessionId);
        return new DataJsonResult<>(true, authentication != null);
    }

    /**
     * 更新实名认证信息
     *
     * @param request
     * @param authentication
     * @param images
     * @return
     * @throws NullIDPhotoException
     * @throws InconsistentAuthenticationException
     * @throws AuthenticationRecordExistException
     * @throws IDPhotoCountLimitationException
     * @throws IDPhotoSizeLimitationException
     */
    @RequestMapping(value = "/api/authentication/update", method = RequestMethod.POST)
    public JsonResult updateAuthentication(HttpServletRequest request
            , @Validated Authentication authentication
            , @Nullable MultipartFile[] images) throws NullIDPhotoException, InconsistentAuthenticationException, AuthenticationRecordExistException, IDPhotoCountLimitationException, IDPhotoSizeLimitationException {
        String sessionId = (String) request.getAttribute("sessionId");
        authenticationService.updateAuthentication(sessionId, authentication, images);
        return new JsonResult(true);
    }

    /**
     * 取消实名认证
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/api/authentication/delete", method = RequestMethod.POST)
    public JsonResult deleteAuthentication(HttpServletRequest request) {
        String sessionId = (String) request.getAttribute("sessionId");
        authenticationService.deleteAuthentication(sessionId);
        return new JsonResult(true);
    }
}

