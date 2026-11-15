package cn.gdeiassistant.core.userlogin.service;

import cn.gdeiassistant.common.exception.commonexception.PasswordIncorrectException;
import cn.gdeiassistant.core.userdata.service.UserDataService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.env.MockEnvironment;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UserLoginServiceTest {

    @Mock
    private UserCertificateService userCertificateService;

    @Mock
    private UserDataService userDataService;

    @InjectMocks
    private UserLoginService userLoginService;

    @Test
    void loginAlwaysUsesRemoteSchoolFlowForCampusCredentialLogin() throws Exception {
        userLoginService.userLogin("session-1", "campus-user", "raw-password", false);

        verify(userCertificateService).syncUpdateSessionCertificate("session-1", "campus-user", "raw-password");
        verify(userCertificateService).saveUserLoginCertificate("session-1", "campus-user", "raw-password");
        verify(userDataService).syncUserData(any(), eq(false));
    }

    @Test
    void loginPropagatesPersistCredentialFlagToUserDataSync() throws Exception {
        userLoginService.userLogin("session-1", "campus-user", "raw-password", true);

        verify(userCertificateService).syncUpdateSessionCertificate("session-1", "campus-user", "raw-password");
        verify(userCertificateService).saveUserLoginCertificate("session-1", "campus-user", "raw-password");
        verify(userDataService).syncUserData(any(), eq(true));
    }

    @Test
    void stagingDemoAccountSkipsRemoteCasWhenEnabledAndCredentialsMatch() throws Exception {
        MockEnvironment env = new MockEnvironment();
        env.setActiveProfiles("staging");
        UserLoginService service = new UserLoginService(userCertificateService, userDataService);
        service.configureDemoAccount(env, true, "demo-student", "demo-token-value");

        service.userLogin("session-demo", "demo-student", "demo-token-value", false);

        verify(userCertificateService, never()).syncUpdateSessionCertificate(anyString(), anyString(), anyString());
        verify(userDataService).syncUserData(any(), eq(false));
        verify(userCertificateService).saveUserLoginCertificate("session-demo", "demo-student", "demo-token-value");
    }

    @Test
    void stagingDemoAccountRejectsWrongPasswordWithoutCallingRemoteCas() throws Exception {
        MockEnvironment env = new MockEnvironment();
        env.setActiveProfiles("staging");
        UserLoginService service = new UserLoginService(userCertificateService, userDataService);
        service.configureDemoAccount(env, true, "demo-student", "demo-token-value");

        assertThrows(PasswordIncorrectException.class,
                () -> service.userLogin("session-demo", "demo-student", "wrong-value", false));

        verify(userCertificateService, never()).syncUpdateSessionCertificate(anyString(), anyString(), anyString());
        verify(userDataService, never()).syncUserData(any(), eq(false));
    }

    @Test
    void productionProfileForcesRemoteCasEvenIfDemoConfigIsEnabled() throws Exception {
        MockEnvironment env = new MockEnvironment();
        env.setActiveProfiles("production");
        UserLoginService service = new UserLoginService(userCertificateService, userDataService);
        service.configureDemoAccount(env, true, "demo-student", "demo-token-value");

        service.userLogin("session-prod", "demo-student", "demo-token-value", false);

        verify(userCertificateService).syncUpdateSessionCertificate("session-prod", "demo-student", "demo-token-value");
        verify(userDataService).syncUserData(any(), eq(false));
        verify(userCertificateService).saveUserLoginCertificate("session-prod", "demo-student", "demo-token-value");
    }
}
