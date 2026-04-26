package cn.gdeiassistant.core.userLogin.service;

import cn.gdeiassistant.core.userData.service.UserDataService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
}
