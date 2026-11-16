package cn.gdeiassistant.core.userlogin.service;

import cn.gdeiassistant.common.exception.commonexception.PasswordIncorrectException;
import cn.gdeiassistant.common.i18n.Messages;
import cn.gdeiassistant.common.pojo.entity.User;
import cn.gdeiassistant.core.demo.DemoAccount;
import cn.gdeiassistant.core.userdata.service.UserDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserLoginService {

    private final cn.gdeiassistant.core.userlogin.service.UserCertificateService userCertificateService;

    private final UserDataService userDataService;

    private DemoAccount demoAccount;

    public UserLoginService(cn.gdeiassistant.core.userlogin.service.UserCertificateService userCertificateService, UserDataService userDataService) {
        this.userCertificateService = userCertificateService;
        this.userDataService = userDataService;
    }

    @Autowired
    void configureDemoAccount(DemoAccount demoAccount) {
        this.demoAccount = demoAccount;
    }

    /**
     * 用户登录验证
     *
     * @param sessionId
     * @param username
     * @param password
     * @return
     */
    public void userLogin(String sessionId, String username, String password) throws Exception {
        userLogin(sessionId, username, password, true);
    }

    /**
     * 用户登录验证
     *
     * @param sessionId
     * @param username
     * @param password
     * @param persistCredential 是否允许更新长期保存的校园凭证
     */
    public void userLogin(String sessionId, String username, String password, boolean persistCredential) throws Exception {
        if (demoAccount != null && demoAccount.matches(username)) {
            if (!demoAccount.passwordMatches(password)) {
                throw new PasswordIncorrectException(Messages.get("userlogin.loginCredentialsIncorrect"));
            }
            userDataService.syncUserData(new User(username, password), persistCredential);
            userCertificateService.saveUserLoginCertificate(sessionId, username, password);
            return;
        }
        completeRemoteSchoolLogin(sessionId, username, password, persistCredential);
    }

    private void completeRemoteSchoolLogin(String sessionId, String username, String password, boolean persistCredential) throws Exception {
        userCertificateService.syncUpdateSessionCertificate(sessionId, username, password);
        userDataService.syncUserData(new User(username, password), persistCredential);
        userCertificateService.saveUserLoginCertificate(sessionId, username, password);
    }

}
