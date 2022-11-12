package cn.gdeiassistant.Controller.Authentication.RestController;

import cn.gdeiassistant.Exception.AuthenticationException.*;
import cn.gdeiassistant.Pojo.Entity.Authentication;
import cn.gdeiassistant.Pojo.Result.DataJsonResult;
import cn.gdeiassistant.Pojo.Result.JsonResult;
import cn.gdeiassistant.Service.AccountManagement.Authentication.AuthenticationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;

@RestController
public class AuthenticationRestController {

    @Autowired
    private AuthenticationService authenticationService;

    /**
     * 查询实名认证状态
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/api/authentication/status", method = RequestMethod.GET)
    public DataJsonResult<Boolean> GetAuthenticationStatus(HttpServletRequest request) {
        Authentication authentication = authenticationService.QueryAuthentication(request.getSession().getId());
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
    public JsonResult UpdateAuthentication(HttpServletRequest request
            , @Validated Authentication authentication
            , @Nullable MultipartFile[] images) throws NullIDPhotoException, InconsistentAuthenticationException, AuthenticationRecordExistException, IDPhotoCountLimitationException, IDPhotoSizeLimitationException {
        authenticationService.UpdateAuthentication(request.getSession().getId()
                , authentication, images);
        return new JsonResult(true);
    }

    /**
     * 取消实名认证
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/api/authentication/delete", method = RequestMethod.POST)
    public JsonResult DeleteAuthentication(HttpServletRequest request) {
        authenticationService.DeleteAuthentication(request.getSession().getId());
        return new JsonResult(true);
    }
}

