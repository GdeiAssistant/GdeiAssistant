package cn.gdeiassistant.core.userLogin.service;

import cn.gdeiassistant.core.campuscredential.service.CampusCredentialService;
import cn.gdeiassistant.core.user.mapper.UserMapper;
import cn.gdeiassistant.core.user.pojo.entity.UserEntity;
import cn.gdeiassistant.core.userData.service.UserDataService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserLoginServiceTest {

    @Mock
    private UserMapper userMapper;

    @Mock
    private UserCertificateService userCertificateService;

    @Mock
    private UserDataService userDataService;

    @Mock
    private CampusCredentialService campusCredentialService;

    @InjectMocks
    private UserLoginService userLoginService;

    @Test
    void noActiveConsentDoesNotUseSavedCredentialShortcut() throws Exception {
        UserEntity userEntity = new UserEntity();
        userEntity.setUsername("campus-user");
        userEntity.setPassword("raw-password");

        when(userMapper.selectUser("campus-user")).thenReturn(userEntity);
        when(campusCredentialService.isEffectiveQuickAuthEnabled("campus-user")).thenReturn(false);

        userLoginService.userLogin("session-1", "campus-user", "raw-password", false);

        verify(userCertificateService, never()).saveUserLoginCertificate(anyString(), anyString(), anyString());
        verify(userCertificateService).syncUpdateSessionCertificate("session-1", "campus-user", "raw-password");
        verify(userDataService).syncUserData(any(), eq(false));
    }

    @Test
    void activeConsentAndSavedCredentialAllowSavedCredentialShortcut() throws Exception {
        UserEntity userEntity = new UserEntity();
        userEntity.setUsername("campus-user");
        userEntity.setPassword("raw-password");

        when(userMapper.selectUser("campus-user")).thenReturn(userEntity);
        when(campusCredentialService.isEffectiveQuickAuthEnabled("campus-user")).thenReturn(true);

        userLoginService.userLogin("session-1", "campus-user", "raw-password", true);

        verify(userCertificateService).saveUserLoginCertificate("session-1", "campus-user", "raw-password");
        verify(userCertificateService, never()).syncUpdateSessionCertificate(anyString(), anyString(), anyString());
        verify(userDataService, never()).syncUserData(any(), anyBoolean());
    }
}
