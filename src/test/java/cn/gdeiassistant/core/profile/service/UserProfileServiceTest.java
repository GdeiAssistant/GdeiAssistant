package cn.gdeiassistant.core.profile.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import cn.gdeiassistant.common.exception.databaseexception.UserNotExistException;
import cn.gdeiassistant.common.exception.tokenvalidexception.TokenExpiredException;
import cn.gdeiassistant.common.pojo.entity.*;
import cn.gdeiassistant.common.tools.springutils.R2StorageService;
import cn.gdeiassistant.core.profile.converter.UserProfileMapper;
import cn.gdeiassistant.core.profile.mapper.ProfileMapper;
import cn.gdeiassistant.core.profile.pojo.entity.ProfileEntity;
import cn.gdeiassistant.core.userlogin.service.UserCertificateService;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mapstruct.factory.Mappers;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.*;
import java.time.*;
import java.util.concurrent.TimeUnit;

@ExtendWith(MockitoExtension.class)
class UserProfileServiceTest {
    @Mock UserCertificateService certificates;
    @Mock ProfileMapper profiles;
    @Mock R2StorageService storage;
    @Mock cn.gdeiassistant.core.objectstorage.service.UploadService uploadService;

    @Mock
    private cn.gdeiassistant.core.objectstorage.service.StoredAssetService storedAssets;
    @InjectMocks UserProfileService service;

    @BeforeEach
    void setup() {
        ReflectionTestUtils.setField(
                service, "userProfileMapper", Mappers.getMapper(UserProfileMapper.class));
    }

    ProfileEntity entity() {
        ProfileEntity p = new ProfileEntity();
        p.setUsername("owner");
        p.setNickname("Synthetic nickname");
        p.setFaculty(1);
        return p;
    }

    void login() {
        when(certificates.getUserLoginCertificate("session")).thenReturn(new User("owner"));
    }

    @Test
    void profileAndIntroductionReadUseCurrentOwnerAndMappedView() throws Exception {
        login();
        when(profiles.selectUserProfile("owner")).thenReturn(entity());
        var p = service.getSelfUserProfile("session");
        assertEquals("Synthetic nickname", p.getNickname());
        assertEquals(1, p.getFaculty());
        Introduction i = new Introduction();
        i.setIntroductionContent("Synthetic biography");
        when(profiles.selectUserIntroduction("owner")).thenReturn(i);
        assertSame(i, service.getSelfUserIntroduction("session"));
        assertSame(i, service.getOtherUserIntroduction("owner"));
        assertThrows(UserNotExistException.class, () -> service.getUserProfile("missing"));
        assertThrows(
                UserNotExistException.class, () -> service.getOtherUserIntroduction("missing"));
    }

    @Test
    void allAvatarOperationsUseOwnerKeysAndBoundedUrls() throws Exception {
        login();
        when(storedAssets.generatePresignedUrl(
                        anyString(), eq(30L), eq(TimeUnit.MINUTES)))
                .thenAnswer(i -> "https://synthetic.invalid/" + i.getArgument(0));
        assertTrue(service.getSelfUserAvatar("session").endsWith("avatar/owner.jpg"));
        assertTrue(
                service.getSelfUserHighDefinitionAvatar("session").endsWith("avatar/owner_hd.jpg"));
        service.updateAvatarByObjectKey("session", "uploads/image");
        service.updateHighDefinitionAvatarByObjectKey("session", "uploads/hd");
        verify(uploadService).moveUpload("session", "uploads/image", "avatar/owner.jpg");
        verify(uploadService).moveUpload("session", "uploads/hd", "avatar/owner_hd.jpg");
        service.deleteAvatar("session");
        verify(storedAssets).deleteObject("avatar/owner.jpg");
        verify(storedAssets).deleteObject("avatar/owner_hd.jpg");
        assertThrows(IllegalArgumentException.class, () -> service.deleteAvatarForUsername(" "));
    }

    @Test
    void avatarUploadsCloseOwnedStreams() throws Exception {
        login();
        InputStream normal = mock(InputStream.class), hd = mock(InputStream.class);
        service.updateAvatar("session", normal);
        service.updateHighDefinitionAvatar("session", hd);
        verify(storedAssets).uploadObject("avatar/owner.jpg", normal);
        verify(storedAssets).uploadObject("avatar/owner_hd.jpg", hd);
        verify(normal).close();
        verify(hd).close();
    }

    @Test
    void introductionInitializesOnlyWhenAbsent() throws Exception {
        login();
        service.updateIntroduction("session", "new");
        verify(profiles).initUserIntroduction("owner");
        verify(profiles).updateUserIntroduction("owner", "new");
        when(profiles.selectUserIntroduction("owner")).thenReturn(new Introduction());
        service.updateIntroduction("session", "changed");
        verify(profiles, times(1)).initUserIntroduction("owner");
        verify(profiles).updateUserIntroduction("owner", "changed");
    }

    @Test
    void profileMutationsPersistOnlyTheAuthenticatedEntity() throws Exception {
        login();
        ProfileEntity p = entity();
        when(profiles.selectUserProfile("owner")).thenReturn(p);
        service.updateLocation("session", "CN", "ST", "CT");
        assertEquals("CN", p.getLocationRegion());
        assertEquals("ST", p.getLocationState());
        assertEquals("CT", p.getLocationCity());
        verify(profiles).updateLocation(p);
        service.updateHometown("session", "CN", "HS", "HC");
        assertEquals("HS", p.getHometownState());
        assertEquals("HC", p.getHometownCity());
        verify(profiles).updateHometown(p);
        service.updateBirthday("session", 2000, 2, 29);
        assertEquals(
                LocalDate.of(2000, 2, 29),
                p.getBirthday().toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
        service.resetBirthday("session");
        assertNull(p.getBirthday());
        verify(profiles, times(2)).updateBirthday(p);
        service.updateFaculty("session", 2);
        assertEquals(2, p.getFaculty());
        verify(profiles).updateFaculty(p);
        service.updateMajor("session", "Software");
        assertEquals("Software", p.getMajor());
        verify(profiles).updateMajor(p);
        service.updateEnrollment("session", 2026);
        assertEquals(2026, p.getEnrollment());
        service.resetEnrollment("session");
        assertNull(p.getEnrollment());
        verify(profiles, times(2)).updateEnrollment(p);
        service.updateNickname("session", "New");
        assertEquals("New", p.getNickname());
        verify(profiles).updateNickname(p);
    }

    @Test
    void invalidBirthdayCannotReachPersistence() throws Exception {
        login();
        when(profiles.selectUserProfile("owner")).thenReturn(entity());
        assertThrows(DateTimeException.class, () -> service.updateBirthday("session", 2001, 2, 29));
        verify(profiles, never()).updateBirthday(any());
    }

    @ParameterizedTest
    @ValueSource(
            strings = {
                "getSelfUserProfile",
                "getSelfUserIntroduction",
                "getSelfUserAvatar",
                "getSelfUserHighDefinitionAvatar",
                "deleteAvatar",
                "resetBirthday",
                "resetEnrollment"
            })
    void expiredSessionsRejectAllSelfOperations(String method) {
        java.lang.reflect.InvocationTargetException error =
                assertThrows(
                        java.lang.reflect.InvocationTargetException.class,
                        () ->
                                UserProfileService.class
                                        .getMethod(method, String.class)
                                        .invoke(service, "expired"));
        assertInstanceOf(TokenExpiredException.class, error.getCause());
        verifyNoInteractions(profiles, storage);
    }

    @Test
    void missingProfilesRejectAllMutations() throws Exception {
        login();
        assertThrows(
                UserNotExistException.class,
                () -> service.updateLocation("session", "CN", "ST", "CT"));
        assertThrows(
                UserNotExistException.class,
                () -> service.updateHometown("session", "CN", "ST", "CT"));
        assertThrows(
                UserNotExistException.class, () -> service.updateBirthday("session", 2000, 1, 1));
        assertThrows(UserNotExistException.class, () -> service.resetBirthday("session"));
        assertThrows(UserNotExistException.class, () -> service.updateFaculty("session", 1));
        assertThrows(UserNotExistException.class, () -> service.updateMajor("session", "Software"));
        assertThrows(UserNotExistException.class, () -> service.updateEnrollment("session", 2026));
        assertThrows(UserNotExistException.class, () -> service.resetEnrollment("session"));
        assertThrows(UserNotExistException.class, () -> service.updateNickname("session", "New"));
    }
}
