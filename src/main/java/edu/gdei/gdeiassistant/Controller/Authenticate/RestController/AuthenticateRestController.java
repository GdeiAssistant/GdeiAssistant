package edu.gdei.gdeiassistant.Controller.Authenticate.RestController;

import edu.gdei.gdeiassistant.Annotation.UserGroupAccess;
import edu.gdei.gdeiassistant.Enum.Authentication.AuthenticationTypeEnum;
import edu.gdei.gdeiassistant.Enum.UserGroup.UserGroupEnum;
import edu.gdei.gdeiassistant.Exception.CommonException.ServerErrorException;
import edu.gdei.gdeiassistant.Pojo.Entity.Authentication;
import edu.gdei.gdeiassistant.Pojo.Entity.Identity;
import edu.gdei.gdeiassistant.Pojo.Entity.Phone;
import edu.gdei.gdeiassistant.Pojo.Entity.User;
import edu.gdei.gdeiassistant.Pojo.Result.DataJsonResult;
import edu.gdei.gdeiassistant.Pojo.Result.JsonResult;
import edu.gdei.gdeiassistant.Service.Authenticate.AuthenticateDataService;
import edu.gdei.gdeiassistant.Service.Authenticate.AuthenticateService;
import edu.gdei.gdeiassistant.Service.Phone.PhoneService;
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
    private PhoneService phoneService;

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
     * 清除实名认证信息
     *
     * @param request
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/api/authentication/remove", method = RequestMethod.POST)
    @UserGroupAccess(group = {2, 3, 6}, rest = true)
    public JsonResult RemoveAuthenticationData(HttpServletRequest request) throws Exception {
        String username = (String) request.getSession().getAttribute("username");
        Authentication authentication = authenticateDataService.QueryAuthenticationData(username);
        if (authentication != null) {
            authenticateDataService.RemoveAuthenticationData(username);
            return new JsonResult(true);
        }
        return new JsonResult(false, "你未进行实名认证");
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
    @UserGroupAccess(group = {2, 3, 6}, rest = true)
    public JsonResult RealNameAuthenticate(HttpServletRequest request
            , @RequestParam(value = "image", required = false) MultipartFile file
            , @RequestParam("method") AuthenticationTypeEnum authenticationTypeEnum) throws Exception {
        String username = (String) request.getSession().getAttribute("username");
        String password = (String) request.getSession().getAttribute("password");
        Integer group = (Integer) request.getSession().getAttribute("group");
        switch (authenticationTypeEnum) {
            //与教务系统进行同步
            case CAS_SYSTEM:
                //获取用户真实姓名
                if (group.equals(UserGroupEnum.STUDENT.getValue()) || group.equals(UserGroupEnum.TEST.getValue())) {
                    Map<String, String> infoMap = authenticateService.GetAuthenticationInfoBySystem(request.getSession().getId(), username, password);
                    String name = infoMap.get("name");
                    String identityNumber = authenticateService.GetUserIdentityNumber(request.getSession().getId(), new User(username, password));
                    //保存用户实名信息
                    authenticateDataService.SaveSystemAuthenticationData(username, name
                            , identityNumber, AuthenticationTypeEnum.CAS_SYSTEM);
                    return new JsonResult(true);
                }
                return new JsonResult(false, "当前用户组不支持使用教务系统进行实名认证");

            //上传身份证照片认证
            case MAINLAND_IDENTITY_CARD:
                if (file == null || file.isEmpty() || file.getSize() == 0) {
                    return new JsonResult(false, "上传的身份证照片不能为空");
                }
                if (file.getSize() > MAX_PICTURE_SIZE) {
                    return new JsonResult(false, "上传的身份证照片大小超过限制");
                }
                Identity identity = authenticateService.ParseIdentityCardInfo(file.getInputStream());
                String name = identity.getName();
                String identityNumber = identity.getCode();
                //校验身份证信息
                authenticateService.VerifyIdentityCard(name, identityNumber);
                authenticateDataService.SaveSystemAuthenticationData(username, name
                        , identityNumber, AuthenticationTypeEnum.MAINLAND_IDENTITY_CARD);
                return new JsonResult(true);

            case PHONE:
                Phone phone = phoneService.QueryUserPhone(username);
                if (phone != null) {
                    authenticateDataService.SaveSystemAuthenticationData(username, phone);
                    return new JsonResult(true);
                }
                return new JsonResult(false, "你未绑定手机号，请先前往个人中心绑定手机号");

            default:
                return new JsonResult(false, "暂不支持当前实名认证方法");
        }
    }
}
