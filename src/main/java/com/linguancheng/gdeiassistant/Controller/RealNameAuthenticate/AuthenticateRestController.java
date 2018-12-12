package com.linguancheng.gdeiassistant.Controller.RealNameAuthenticate;

import com.linguancheng.gdeiassistant.Enum.Authentication.AuthenticationTypeEnum;
import com.linguancheng.gdeiassistant.Exception.CommonException.ServerErrorException;
import com.linguancheng.gdeiassistant.Pojo.Entity.Authentication;
import com.linguancheng.gdeiassistant.Pojo.Entity.User;
import com.linguancheng.gdeiassistant.Pojo.Result.DataJsonResult;
import com.linguancheng.gdeiassistant.Pojo.Result.JsonResult;
import com.linguancheng.gdeiassistant.Service.Authenticate.AuthenticateDataService;
import com.linguancheng.gdeiassistant.Service.Authenticate.AuthenticateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
public class AuthenticateRestController {

    @Autowired
    private AuthenticateService authenticateService;

    @Autowired
    private AuthenticateDataService authenticateDataService;

    /**
     * 获取当前账户实名认证信息
     *
     * @return
     */
    @RequestMapping(value = "/api/authentication", method = RequestMethod.GET)
    public DataJsonResult<Authentication> GetRealNameAuthenticationData(HttpServletRequest request) throws Exception {
        String username = (String) request.getSession().getAttribute("username");
        Authentication authentication = authenticateDataService.QueryAuthenticationData(username);
        return new DataJsonResult<>(true, authentication);
    }

    /**
     * 进行实名认证
     *
     * @param request
     * @param authenticationTypeEnum
     * @return
     * @throws ServerErrorException
     */
    @RequestMapping(value = "/api/authentication", method = RequestMethod.POST)
    public JsonResult RealNameAuthenticate(HttpServletRequest request
            , @RequestParam("method") AuthenticationTypeEnum authenticationTypeEnum) throws Exception {
        String username = (String) request.getSession().getAttribute("username");
        String password = (String) request.getSession().getAttribute("password");
        switch (authenticationTypeEnum) {
            case AUTHENTICATE_WITH_CAS_SYSTEM:
                //获取用户真实姓名
                Map<String, String> infoMap = authenticateService.GetUserRealNameAndSchoolNumber(request.getSession().getId(), username, password);
                String name = infoMap.get("name");
                String number = infoMap.get("number");
                String identityNumber = authenticateService.GetUserIdentityNumber(request.getSession().getId(), new User(username, password));
                authenticateDataService.SaveSystemAuthenticationData(username, name, number, identityNumber);
                return new JsonResult(true);

            case AUTHENTICATE_WITH_UPLOAD_IDENTITY_CARD:
                return new JsonResult(false, "暂不支持当前实名认证方法");

            default:
                return new JsonResult(false, "暂不支持当前实名认证方法");
        }
    }
}
