package cn.gdeiassistant.core.userlogin.service;

import cn.gdeiassistant.common.exception.commonexception.PasswordIncorrectException;
import cn.gdeiassistant.common.i18n.Messages;
import cn.gdeiassistant.common.pojo.entity.User;
import cn.gdeiassistant.core.userdata.service.UserDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;

@Service
public class UserLoginService {

    private final cn.gdeiassistant.core.userlogin.service.UserCertificateService userCertificateService;

    private final UserDataService userDataService;

    private Environment environment;

    private boolean demoAccountEnabled;

    private String demoUsername = "";

    private String demoPassword = "";

    public UserLoginService(cn.gdeiassistant.core.userlogin.service.UserCertificateService userCertificateService, UserDataService userDataService) {
        this.userCertificateService = userCertificateService;
        this.userDataService = userDataService;
    }

    @Autowired(required = false)
    void configureDemoAccount(
            Environment environment,
            @Value("${app.demo.account-enabled:false}") boolean demoAccountEnabled,
            @Value("${app.demo.username:}") String demoUsername,
            @Value("${app.demo.password:}") String demoPassword) {
        this.environment = environment;
        this.demoAccountEnabled = demoAccountEnabled;
        this.demoUsername = demoUsername == null ? "" : demoUsername;
        this.demoPassword = demoPassword == null ? "" : demoPassword;
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
        if (isDemoAccountActive() && constantTimeEquals(username, demoUsername)) {
            if (!constantTimeEquals(password, demoPassword)) {
                throw new PasswordIncorrectException(Messages.get("userlogin.loginCredentialsIncorrect"));
            }
            userDataService.syncUserData(new User(username, password), persistCredential);
            userCertificateService.saveUserLoginCertificate(sessionId, username, password);
            return;
        }
        completeRemoteSchoolLogin(sessionId, username, password, persistCredential);
    }

    private boolean isDemoAccountActive() {
        if (!demoAccountEnabled || demoUsername.isBlank() || demoPassword.isBlank()) {
            return false;
        }
        if (environment != null && Arrays.stream(environment.getActiveProfiles())
                .anyMatch(profile -> "production".equalsIgnoreCase(profile))) {
            return false;
        }
        return true;
    }

    private static boolean constantTimeEquals(String a, String b) {
        if (a == null || b == null) {
            return false;
        }
        return MessageDigest.isEqual(
                a.getBytes(StandardCharsets.UTF_8),
                b.getBytes(StandardCharsets.UTF_8));
    }

    private void completeRemoteSchoolLogin(String sessionId, String username, String password, boolean persistCredential) throws Exception {
        userCertificateService.syncUpdateSessionCertificate(sessionId, username, password);
        userDataService.syncUserData(new User(username, password), persistCredential);
        userCertificateService.saveUserLoginCertificate(sessionId, username, password);
    }

}
