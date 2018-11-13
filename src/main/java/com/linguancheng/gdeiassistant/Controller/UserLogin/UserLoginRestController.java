package com.linguancheng.gdeiassistant.Controller.UserLogin;

import com.linguancheng.gdeiassistant.Enum.Base.DataBaseResultEnum;
import com.linguancheng.gdeiassistant.Enum.Base.LoginMethodEnum;
import com.linguancheng.gdeiassistant.Pojo.Entity.*;
import com.linguancheng.gdeiassistant.Pojo.Result.BaseResult;
import com.linguancheng.gdeiassistant.Pojo.UserLogin.UserCertificate;
import com.linguancheng.gdeiassistant.Pojo.UserLogin.UserLoginJsonResult;
import com.linguancheng.gdeiassistant.Service.IPAddress.IPService;
import com.linguancheng.gdeiassistant.Service.Profile.UserProfileService;
import com.linguancheng.gdeiassistant.Service.Token.LoginTokenService;
import com.linguancheng.gdeiassistant.Service.UserData.UserDataService;
import com.linguancheng.gdeiassistant.Service.UserLogin.UserLoginService;
import com.linguancheng.gdeiassistant.ValidGroup.User.UserLoginValidGroup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

@RestController
public class UserLoginRestController {

    @Autowired
    private UserLoginService userLoginService;

    @Autowired
    private UserDataService userDataService;

    @Autowired
    private UserProfileService userProfileService;

    @Autowired
    private LoginTokenService loginTokenService;

    @Autowired
    private IPService ipService;

    /**
     * 用户登录接口
     *
     * @param request
     * @param user
     * @param unionId
     * @param method
     * @return
     */
    @RequestMapping(value = "/rest/userlogin", method = RequestMethod.POST)
    @ResponseBody
    public UserLoginJsonResult UserLogin(HttpServletRequest request, @Validated(value = UserLoginValidGroup.class) User user
            , @RequestParam("unionId") String unionId, @RequestParam(value = "method"
            , required = false, defaultValue = "0") LoginMethodEnum method) throws Exception {
        UserLoginJsonResult result = new UserLoginJsonResult();
        UserCertificate userCertificate = null;
        switch (method) {
            case QUICK_LOGIN:
                userCertificate = userLoginService.UserLogin(request.getSession().getId(), user, true);
                break;

            case CAS_LOGIN:
                userCertificate = userLoginService.UserLogin(request.getSession().getId(), user, false);
                break;
        }
        User resultUser = userCertificate.getUser();
        //同步数据库用户数据
        userDataService.SyncUserData(resultUser);
        //获取用户真实姓名
        Profile profile = userProfileService.GetUserProfile(user.getUsername());
        resultUser.setRealname(Optional.ofNullable(profile).map(Profile::getRealname).orElse(""));
        //获取权限令牌和刷新令牌
        AccessToken accessToken = loginTokenService.GetAccessToken(user.getUsername(), ipService.GetRequestRealIPAddress(request), unionId);
        RefreshToken refreshToken = loginTokenService.GetRefreshToken(accessToken);
        result.setSuccess(true);
        result.setUser(resultUser);
        result.setAccessToken(new Token(accessToken));
        result.setRefreshToken(new Token(refreshToken));
        return result;
    }
}
