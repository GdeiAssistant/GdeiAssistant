package edu.gdei.gdeiassistant.Service.Authenticate;

import edu.gdei.gdeiassistant.Enum.Authentication.AuthenticationTypeEnum;
import edu.gdei.gdeiassistant.Pojo.Entity.Authentication;
import edu.gdei.gdeiassistant.Pojo.Entity.Phone;
import edu.gdei.gdeiassistant.Repository.SQL.Mysql.Mapper.GdeiAssistant.Authentication.AuthenticationMapper;
import edu.gdei.gdeiassistant.Tools.StringEncryptUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class AuthenticateDataService {

    @Autowired
    private AuthenticationMapper authenticationMapper;

    /**
     * 查询用户实名认证信息
     *
     * @param username
     * @return
     */
    public Authentication QueryAuthenticationData(String username) throws Exception {
        Authentication authentication = authenticationMapper.selectAuthentication(StringEncryptUtils.encryptString(username));
        if (authentication != null) {
            //解密用户名
            authentication.setUsername(StringEncryptUtils.decryptString(authentication.getUsername()));
            return authentication;
        }
        return null;
    }

    /**
     * 清除用户实名认证信息
     *
     * @param username
     * @throws Exception
     */
    public void RemoveAuthenticationData(String username) throws Exception {
        authenticationMapper.deleteAuthentication(StringEncryptUtils.encryptString(username));
    }

    /**
     * 保存用户实名认证信息
     *
     * @param username
     * @param realname
     * @param identityNumber
     * @param authenticationTypeEnum
     */
    public void SaveSystemAuthenticationData(String username, String realname
            , String identityNumber, AuthenticationTypeEnum authenticationTypeEnum) throws Exception {
        Authentication authentication = new Authentication();
        authentication.setUsername(StringEncryptUtils.encryptString(username));
        String salt = UUID.randomUUID().toString().replace("-", "").substring(0, 6);
        authentication.setIdentityCode(StringEncryptUtils.SHA512HexString(
                StringEncryptUtils.encryptString(username + realname + identityNumber + salt)));
        authentication.setSalt(salt);
        authentication.setType(authenticationTypeEnum.getMethod());
        if (authenticationMapper.selectAuthentication(StringEncryptUtils.encryptString(username)) == null) {
            authenticationMapper.insertAuthentication(authentication);
        } else {
            authenticationMapper.updateAuthentication(authentication);
        }
    }

    /**
     * 保存用户实名认证信息
     *
     * @param phone
     * @throws Exception
     */
    public void SaveSystemAuthenticationData(String username, Phone phone) throws Exception {
        Authentication authentication = new Authentication();
        authentication.setUsername(StringEncryptUtils.encryptString(username));
        String salt = UUID.randomUUID().toString().replace("-", "").substring(0, 6);
        authentication.setIdentityCode(StringEncryptUtils.SHA512HexString(
                StringEncryptUtils.encryptString(username + phone.getCode() + phone.getPhone() + salt)));
        authentication.setSalt(salt);
        authentication.setType(AuthenticationTypeEnum.PHONE.getMethod());
        if (authenticationMapper.selectAuthentication(StringEncryptUtils.encryptString(username)) == null) {
            authenticationMapper.insertAuthentication(authentication);
        } else {
            authenticationMapper.updateAuthentication(authentication);
        }
    }
}
