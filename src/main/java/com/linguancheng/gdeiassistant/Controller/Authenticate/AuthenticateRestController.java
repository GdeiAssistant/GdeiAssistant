package com.linguancheng.gdeiassistant.Controller.Authenticate;

import com.linguancheng.gdeiassistant.Enum.Authentication.AuthenticationTypeEnum;
import com.linguancheng.gdeiassistant.Exception.CommonException.ServerErrorException;
import com.linguancheng.gdeiassistant.Pojo.Entity.Authentication;
import com.linguancheng.gdeiassistant.Pojo.Entity.Identity;
import com.linguancheng.gdeiassistant.Pojo.Entity.User;
import com.linguancheng.gdeiassistant.Pojo.Result.DataJsonResult;
import com.linguancheng.gdeiassistant.Pojo.Result.JsonResult;
import com.linguancheng.gdeiassistant.Service.Authenticate.AuthenticateDataService;
import com.linguancheng.gdeiassistant.Service.Authenticate.AuthenticateService;
import com.linguancheng.gdeiassistant.Tools.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
public class AuthenticateRestController {

    private final int MAX_PICTURE_SIZE = 1024 * 1024 * 5;

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
            , @RequestParam(value = "image", required = false) MultipartFile file
            , @RequestParam("method") AuthenticationTypeEnum authenticationTypeEnum) throws Exception {
        String username = (String) request.getSession().getAttribute("username");
        String password = (String) request.getSession().getAttribute("password");
        switch (authenticationTypeEnum) {
            //与教务系统进行同步
            case AUTHENTICATE_WITH_CAS_SYSTEM:
                //获取用户真实姓名
                Map<String, String> infoMap = authenticateService.GetAuthenticationInfoBySystem(request.getSession().getId(), username, password);
                String name = infoMap.get("name");
                String number = infoMap.get("number");
                String identityNumber = authenticateService.GetUserIdentityNumber(request.getSession().getId(), new User(username, password));
                //保存用户实名信息
                authenticateDataService.SaveSystemAuthenticationData(username, name, number, identityNumber);
                return new JsonResult(true);

            //与易班校方认证信息同步
            case AUTHENTICATE_WITH_YIBAN:
                //获取用户真实姓名和学号
                String yiBanAccessToken = (String) request.getSession().getAttribute("yiBanAccessToken");
                if (StringUtils.isNotBlank(yiBanAccessToken)) {
                    infoMap = authenticateService.GetAuthenticationInfoByYiBan(yiBanAccessToken);
                    name = infoMap.get("name");
                    number = infoMap.get("number");
                    if (StringUtils.isBlank(number)) {
                        return new JsonResult(false, "你的易班账户暂不支持实名认证");
                    }
                    //保存用户实名信息
                    authenticateDataService.SaveSystemAuthenticationData(username, name, number, null);
                    return new JsonResult(true);
                }
                return new JsonResult(false, "用户登录凭证过期，请重新登录");

            //上传身份证照片认证
            case AUTHENTICATE_WITH_UPLOAD_IDENTITY_CARD:
                if (file == null || file.isEmpty() || file.getSize() == 0) {
                    return new JsonResult(false, "上传的身份证照片不能为空");
                }
                if (file.getSize() > MAX_PICTURE_SIZE) {
                    return new JsonResult(false, "上传的身份证照片大小超过限制");
                }
                Identity identity = authenticateService.ParseIdentityCardInfo(file.getInputStream());
                name = identity.getName();
                identityNumber = identity.getCode();
                //保存用户实名信息
                authenticateDataService.SaveSystemAuthenticationData(username, name, null, identityNumber);
                return new JsonResult(true);

            default:
                return new JsonResult(false, "暂不支持当前实名认证方法");
        }
    }
}
