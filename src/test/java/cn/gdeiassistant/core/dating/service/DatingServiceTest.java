package cn.gdeiassistant.core.dating.service;

import cn.gdeiassistant.common.exception.DatabaseException.DataNotExistException;
import cn.gdeiassistant.common.exception.DatabaseException.NoAccessException;
import cn.gdeiassistant.common.exception.DatingException.SelfPickException;
import cn.gdeiassistant.common.pojo.Entity.User;
import cn.gdeiassistant.core.dating.mapper.DatingMapper;
import cn.gdeiassistant.core.dating.pojo.dto.DatingPublishDTO;
import cn.gdeiassistant.core.dating.pojo.entity.DatingProfileEntity;
import cn.gdeiassistant.core.message.service.InteractionNotificationService;
import cn.gdeiassistant.core.userLogin.service.UserCertificateService;
import cn.gdeiassistant.common.tools.SpringUtils.R2StorageService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DatingServiceTest {

    @Mock
    private DatingMapper datingMapper;

    @Mock
    private UserCertificateService userCertificateService;

    @Mock
    private R2StorageService r2StorageService;

    @Mock
    private InteractionNotificationService interactionNotificationService;

    @InjectMocks
    private DatingService datingService;

    @Test
    void queryDatingProfile_throwsDataNotExistExceptionWhenNotFound() {
        when(datingMapper.selectDatingProfileById(99)).thenReturn(null);

        assertThrows(DataNotExistException.class,
                () -> datingService.queryDatingProfile(99));
    }

    @Test
    void verifyRoommateProfileOwner_throwsNoAccessExceptionWhenNonOwner() {
        User user = new User("userA");
        when(userCertificateService.getUserLoginCertificate("session1")).thenReturn(user);

        DatingProfileEntity profile = new DatingProfileEntity();
        profile.setProfileId(1);
        profile.setUsername("userB");
        when(datingMapper.selectDatingProfileById(1)).thenReturn(profile);

        assertThrows(NoAccessException.class,
                () -> datingService.verifyRoommateProfileOwner("session1", 1));
    }

    @Test
    void verifyRoommatePickRequestAccess_throwsSelfPickExceptionWhenPickingOwnProfile() {
        User user = new User("owner");
        when(userCertificateService.getUserLoginCertificate("session1")).thenReturn(user);

        DatingProfileEntity profile = new DatingProfileEntity();
        profile.setProfileId(5);
        profile.setUsername("owner");
        when(datingMapper.selectDatingProfileById(5)).thenReturn(profile);

        assertThrows(SelfPickException.class,
                () -> datingService.verifyRoommatePickRequestAccess("session1", 5));
    }

    @Test
    void uploadPicture_logsErrorButDoesNotThrowOnR2Failure() {
        InputStream stream = new ByteArrayInputStream("fake".getBytes());
        doThrow(new RuntimeException("R2 unavailable"))
                .when(r2StorageService).uploadObject(anyString(), anyString(), any(InputStream.class));

        assertDoesNotThrow(() -> datingService.uploadPicture(1, stream));
    }

    @Test
    void addRoommateProfile_returnsProfileId() {
        User user = new User("testuser");
        when(userCertificateService.getUserLoginCertificate("session1")).thenReturn(user);

        doAnswer(invocation -> {
            DatingProfileEntity entity = invocation.getArgument(0);
            entity.setProfileId(42);
            return null;
        }).when(datingMapper).insertRoommateProfile(any(DatingProfileEntity.class));

        DatingPublishDTO dto = new DatingPublishDTO();
        dto.setNickname("nick");
        dto.setGrade(2);
        dto.setFaculty("CS");
        dto.setHometown("GZ");
        dto.setContent("hello");
        dto.setArea(0);

        Integer id = datingService.addRoommateProfile("session1", dto);

        assertEquals(42, id);
        verify(datingMapper).insertRoommateProfile(any(DatingProfileEntity.class));
    }
}
