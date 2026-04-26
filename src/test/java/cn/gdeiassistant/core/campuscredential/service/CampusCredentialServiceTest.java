package cn.gdeiassistant.core.campuscredential.service;

import cn.gdeiassistant.common.pojo.Entity.User;
import cn.gdeiassistant.core.campuscredential.mapper.CampusCredentialConsentMapper;
import cn.gdeiassistant.core.campuscredential.pojo.dto.CampusCredentialConsentDTO;
import cn.gdeiassistant.core.campuscredential.pojo.entity.CampusCredentialConsentEntity;
import cn.gdeiassistant.core.campuscredential.pojo.vo.CampusCredentialStatusVO;
import cn.gdeiassistant.core.privacy.mapper.PrivacyMapper;
import cn.gdeiassistant.core.privacy.pojo.entity.PrivacyEntity;
import cn.gdeiassistant.core.user.mapper.UserMapper;
import cn.gdeiassistant.core.user.pojo.entity.UserEntity;
import cn.gdeiassistant.core.userLogin.service.UserCertificateService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CampusCredentialServiceTest {

    private static final String USERNAME = "campususer2026";
    private static final String SESSION_ID = "session-1";

    @Mock
    private CampusCredentialConsentMapper campusCredentialConsentMapper;

    @Mock
    private PrivacyMapper privacyMapper;

    @Mock
    private UserMapper userMapper;

    @Mock
    private UserCertificateService userCertificateService;

    @InjectMocks
    private CampusCredentialService campusCredentialService;

    @Test
    void recordConsentCreatesActiveConsentAndEnablesQuickAuth() throws Exception {
        PrivacyEntity privacy = new PrivacyEntity();
        privacy.setUsername(USERNAME);
        privacy.setQuickAuthAllow(true);

        UserEntity userEntity = new UserEntity();
        userEntity.setUsername(USERNAME);
        userEntity.setPassword("saved-password");

        CampusCredentialConsentEntity activeConsent = consent("LOGIN", null);

        when(privacyMapper.selectPrivacy(USERNAME)).thenReturn(privacy, privacy);
        when(campusCredentialConsentMapper.selectLatestActiveConsent(USERNAME, CampusCredentialService.CONSENT_TYPE_CAMPUS_CREDENTIAL))
                .thenReturn(null, activeConsent);
        when(campusCredentialConsentMapper.selectLatestConsent(USERNAME, CampusCredentialService.CONSENT_TYPE_CAMPUS_CREDENTIAL))
                .thenReturn(activeConsent);
        when(userMapper.selectUser(USERNAME)).thenReturn(userEntity);

        CampusCredentialConsentDTO dto = new CampusCredentialConsentDTO();
        dto.setScene("LOGIN");
        dto.setPolicyDate("2026-04-25");
        dto.setEffectiveDate("2026-05-11");

        CampusCredentialStatusVO status = campusCredentialService.recordConsentByUsername(USERNAME, dto);

        verify(campusCredentialConsentMapper).insertConsent(any(CampusCredentialConsentEntity.class));
        verify(privacyMapper).updateQuickAuth(true, USERNAME);
        assertTrue(Boolean.TRUE.equals(status.getHasActiveConsent()));
        assertTrue(Boolean.TRUE.equals(status.getHasSavedCredential()));
        assertTrue(Boolean.TRUE.equals(status.getQuickAuthEnabled()));
        assertNotNull(status.getMaskedCampusAccount());
        assertFalse(status.getMaskedCampusAccount().contains(USERNAME));
    }

    @Test
    void statusWithoutConsentTreatsQuickAuthAsDisabled() {
        PrivacyEntity privacy = new PrivacyEntity();
        privacy.setUsername(USERNAME);
        privacy.setQuickAuthAllow(true);

        UserEntity userEntity = new UserEntity();
        userEntity.setUsername(USERNAME);
        userEntity.setPassword("legacy-saved-password");

        when(privacyMapper.selectPrivacy(USERNAME)).thenReturn(privacy);
        when(userMapper.selectUser(USERNAME)).thenReturn(userEntity);
        when(campusCredentialConsentMapper.selectLatestConsent(USERNAME, CampusCredentialService.CONSENT_TYPE_CAMPUS_CREDENTIAL))
                .thenReturn(null);
        when(campusCredentialConsentMapper.selectLatestActiveConsent(USERNAME, CampusCredentialService.CONSENT_TYPE_CAMPUS_CREDENTIAL))
                .thenReturn(null);

        CampusCredentialStatusVO status = campusCredentialService.getStatusByUsername(USERNAME);

        assertFalse(Boolean.TRUE.equals(status.getHasActiveConsent()));
        assertTrue(Boolean.TRUE.equals(status.getHasSavedCredential()));
        assertFalse(Boolean.TRUE.equals(status.getQuickAuthEnabled()));
    }

    @Test
    void revokeConsentDisablesQuickAuthAndClearsSavedCredentialState() throws Exception {
        User user = new User(USERNAME, "runtime-password");
        PrivacyEntity privacy = new PrivacyEntity();
        privacy.setUsername(USERNAME);
        privacy.setQuickAuthAllow(false);

        UserEntity userEntity = new UserEntity();
        userEntity.setUsername(USERNAME);
        userEntity.setPassword(null);

        CampusCredentialConsentEntity revokedConsent = consent("SETTINGS", new java.util.Date());

        when(userCertificateService.getUserLoginCertificate(SESSION_ID)).thenReturn(user);
        when(privacyMapper.selectPrivacy(USERNAME)).thenReturn(privacy);
        when(userMapper.selectUser(USERNAME)).thenReturn(userEntity);
        when(campusCredentialConsentMapper.selectLatestConsent(USERNAME, CampusCredentialService.CONSENT_TYPE_CAMPUS_CREDENTIAL))
                .thenReturn(revokedConsent);
        when(campusCredentialConsentMapper.selectLatestActiveConsent(USERNAME, CampusCredentialService.CONSENT_TYPE_CAMPUS_CREDENTIAL))
                .thenReturn(null);

        CampusCredentialStatusVO status = campusCredentialService.revokeConsent(SESSION_ID);

        verify(campusCredentialConsentMapper).revokeActiveConsents(eq(USERNAME), eq(CampusCredentialService.CONSENT_TYPE_CAMPUS_CREDENTIAL), any(java.util.Date.class), eq("USER_REVOKED"));
        verify(privacyMapper).updateQuickAuth(false, USERNAME);
        verify(userMapper).clearPassword(USERNAME);
        verify(userCertificateService).clearReusableCredentials(USERNAME);
        verify(userCertificateService).clearUserLoginAndSession(SESSION_ID);
        assertFalse(Boolean.TRUE.equals(status.getHasActiveConsent()));
        assertFalse(Boolean.TRUE.equals(status.getHasSavedCredential()));
        assertFalse(Boolean.TRUE.equals(status.getQuickAuthEnabled()));
    }

    @Test
    void deleteSavedCredentialIsIdempotentWithoutActiveConsent() throws Exception {
        User user = new User(USERNAME, "runtime-password");
        PrivacyEntity privacy = new PrivacyEntity();
        privacy.setUsername(USERNAME);
        privacy.setQuickAuthAllow(false);

        UserEntity userEntity = new UserEntity();
        userEntity.setUsername(USERNAME);
        userEntity.setPassword(null);

        when(userCertificateService.getUserLoginCertificate(SESSION_ID)).thenReturn(user);
        when(privacyMapper.selectPrivacy(USERNAME)).thenReturn(privacy);
        when(userMapper.selectUser(USERNAME)).thenReturn(userEntity);
        when(campusCredentialConsentMapper.selectLatestConsent(USERNAME, CampusCredentialService.CONSENT_TYPE_CAMPUS_CREDENTIAL))
                .thenReturn(null);
        when(campusCredentialConsentMapper.selectLatestActiveConsent(USERNAME, CampusCredentialService.CONSENT_TYPE_CAMPUS_CREDENTIAL))
                .thenReturn(null);

        CampusCredentialStatusVO status = campusCredentialService.deleteSavedCredential(SESSION_ID);

        verify(campusCredentialConsentMapper).revokeActiveConsents(eq(USERNAME), eq(CampusCredentialService.CONSENT_TYPE_CAMPUS_CREDENTIAL), any(java.util.Date.class), eq("CREDENTIAL_DELETED"));
        verify(userMapper).clearPassword(USERNAME);
        verify(userCertificateService).clearReusableCredentials(USERNAME);
        verify(userCertificateService).clearUserLoginAndSession(SESSION_ID);
        assertFalse(Boolean.TRUE.equals(status.getHasSavedCredential()));
        assertFalse(Boolean.TRUE.equals(status.getQuickAuthEnabled()));
    }

    @Test
    void enablingQuickAuthFailsWithoutActiveConsent() {
        User user = new User(USERNAME, "runtime-password");
        when(userCertificateService.getUserLoginCertificate(SESSION_ID)).thenReturn(user);
        when(campusCredentialConsentMapper.selectLatestActiveConsent(USERNAME, CampusCredentialService.CONSENT_TYPE_CAMPUS_CREDENTIAL))
                .thenReturn(null);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> campusCredentialService.updateQuickAuth(SESSION_ID, true));
        assertTrue(exception.getMessage().contains("授权"));
        verify(privacyMapper, never()).updateQuickAuth(true, USERNAME);
    }

    @Test
    void enablingQuickAuthFailsWithoutSavedCredential() {
        User user = new User(USERNAME, "runtime-password");
        PrivacyEntity privacy = new PrivacyEntity();
        privacy.setUsername(USERNAME);
        privacy.setQuickAuthAllow(false);

        UserEntity userEntity = new UserEntity();
        userEntity.setUsername(USERNAME);
        userEntity.setPassword(null);

        CampusCredentialConsentEntity activeConsent = consent("LOGIN", null);

        when(userCertificateService.getUserLoginCertificate(SESSION_ID)).thenReturn(user);
        when(privacyMapper.selectPrivacy(USERNAME)).thenReturn(privacy);
        when(userMapper.selectUser(USERNAME)).thenReturn(userEntity);
        when(campusCredentialConsentMapper.selectLatestActiveConsent(USERNAME, CampusCredentialService.CONSENT_TYPE_CAMPUS_CREDENTIAL))
                .thenReturn(activeConsent);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> campusCredentialService.updateQuickAuth(SESSION_ID, true));
        assertTrue(exception.getMessage().contains("校园凭证"));
        verify(privacyMapper, never()).updateQuickAuth(true, USERNAME);
    }

    @Test
    void filterUsersWithEffectiveQuickAuthExcludesRevokedAndDisabledUsers() {
        User enabledUser = new User("enabled-user");
        User noConsentUser = new User("no-consent-user");
        User disabledUser = new User("disabled-user");

        CampusCredentialService service = spy(campusCredentialService);
        doReturn(true).when(service).isEffectiveQuickAuthEnabled("enabled-user");
        doReturn(false).when(service).isEffectiveQuickAuthEnabled("no-consent-user");
        doReturn(false).when(service).isEffectiveQuickAuthEnabled("disabled-user");

        List<User> filtered = service.filterUsersWithEffectiveQuickAuth(
                List.of(enabledUser, noConsentUser, disabledUser)
        );

        assertEquals(1, filtered.size());
        assertEquals("enabled-user", filtered.get(0).getUsername());
    }

    private CampusCredentialConsentEntity consent(String scene, java.util.Date revokedAt) {
        CampusCredentialConsentEntity entity = new CampusCredentialConsentEntity();
        entity.setUsername(USERNAME);
        entity.setConsentType(CampusCredentialService.CONSENT_TYPE_CAMPUS_CREDENTIAL);
        entity.setPolicyDate(Date.valueOf("2026-04-25"));
        entity.setEffectiveDate(Date.valueOf("2026-05-11"));
        entity.setScene(scene);
        entity.setConsentedAt(new java.util.Date());
        entity.setRevokedAt(revokedAt);
        return entity;
    }
}
