package edu.gdei.gdeiassistant.Service.Authenticate;

import edu.gdei.gdeiassistant.Enum.Authentication.AuthenticationTypeEnum;
import edu.gdei.gdeiassistant.Pojo.Entity.Authentication;
import edu.gdei.gdeiassistant.Repository.Mysql.GdeiAssistant.Authentication.AuthenticationMapper;
import edu.gdei.gdeiassistant.Tools.StringEncryptUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

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
     * 保存用户实名认证信息
     *
     * @param username
     * @param realname
     * @param schoolNumber
     * @param identityNumber
     */
    public void SaveSystemAuthenticationData(String username, String realname, String schoolNumber
            , String identityNumber) throws Exception {
        Authentication authentication = new Authentication();
        authentication.setUsername(StringEncryptUtils.encryptString(username));
        authentication.setRealname(realname);
        authentication.setSchoolNumber(schoolNumber);
        authentication.setIdentityNumber(identityNumber);
        authentication.setIdentityCode(StringEncryptUtils.SHA1HexString(realname + "."
                + Optional.ofNullable(schoolNumber).orElse("") + "."
                + Optional.ofNullable(identityNumber).orElse("")));
        //隐藏姓名信息
        if (authentication.getRealname().length() == 2) {
            authentication.setRealname(authentication.getRealname().replaceFirst(authentication
                    .getRealname().substring(1), "*"));
        }
        if (authentication.getRealname().length() > 2) {
            StringBuilder stringBuilder = new StringBuilder(authentication.getRealname().substring(0, 1));
            for (int i = 1; i < authentication.getRealname().length() - 1; i++) {
                stringBuilder.append("*");
            }
            stringBuilder.append(authentication.getRealname().substring(authentication.getRealname().length() - 1));
            authentication.setRealname(stringBuilder.toString());
        }
        //隐藏身份证号
        if (authentication.getIdentityNumber() != null) {
            StringBuilder identityNumberStringBuilder = new StringBuilder(authentication.getIdentityNumber().substring(0, 3));
            for (int i = 4; i < authentication.getIdentityNumber().length() - 3; i++) {
                identityNumberStringBuilder.append("*");
            }
            identityNumberStringBuilder.append(authentication.getIdentityNumber()
                    .substring(authentication.getIdentityNumber().length() - 4));
            authentication.setIdentityNumber(identityNumberStringBuilder.toString());
        }
        //隐藏学号
        if (authentication.getSchoolNumber() != null) {
            StringBuilder schoolNumberStringBuilder = new StringBuilder(authentication.getSchoolNumber().substring(0, 2));
            for (int i = 3; i < authentication.getSchoolNumber().length() - 1; i++) {
                schoolNumberStringBuilder.append("*");
            }
            schoolNumberStringBuilder.append(authentication.getSchoolNumber()
                    .substring(authentication.getSchoolNumber().length() - 2));
            authentication.setSchoolNumber(schoolNumberStringBuilder.toString());
        }
        authentication.setMethod(Integer.valueOf(AuthenticationTypeEnum.AUTHENTICATE_WITH_CAS_SYSTEM.getType()));
        if (authenticationMapper.selectAuthentication(StringEncryptUtils.encryptString(username)) == null) {
            authenticationMapper.insertAuthentication(authentication);
        } else {
            authenticationMapper.updateAuthentication(authentication);
        }
    }

    /**
     * 删除用户实名认证信息
     *
     * @param username
     */
    public void DeleteAuthenticationData(String username) throws Exception {
        authenticationMapper.deleteAuthentication(StringEncryptUtils.encryptString(username));
    }

}
