package cn.gdeiassistant.core.photograph.service;

import cn.gdeiassistant.common.exception.DatabaseException.DataNotExistException;
import cn.gdeiassistant.common.pojo.Entity.User;
import cn.gdeiassistant.core.message.service.InteractionNotificationService;
import cn.gdeiassistant.core.photograph.converter.PhotographCommentConverter;
import cn.gdeiassistant.core.photograph.converter.PhotographConverter;
import cn.gdeiassistant.core.photograph.mapper.PhotographMapper;
import cn.gdeiassistant.core.photograph.pojo.entity.PhotographCommentEntity;
import cn.gdeiassistant.core.photograph.pojo.entity.PhotographEntity;
import cn.gdeiassistant.core.photograph.pojo.vo.PhotographVO;
import cn.gdeiassistant.core.userLogin.service.UserCertificateService;
import cn.gdeiassistant.common.tools.SpringUtils.R2StorageService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PhotographServiceTest {

    @Mock
    private PhotographMapper photographMapper;

    @Mock
    private PhotographConverter photographConverter;

    @Mock
    private PhotographCommentConverter photographCommentConverter;

    @Mock
    private UserCertificateService userCertificateService;

    @Mock
    private R2StorageService r2StorageService;

    @Mock
    private InteractionNotificationService interactionNotificationService;

    @InjectMocks
    private PhotographService photographService;

    @Test
    void addPhotographComment_throwsForEmptyComment() {
        assertThrows(IllegalArgumentException.class,
                () -> photographService.addPhotographComment(1, "", "session1"));
    }

    @Test
    void addPhotographComment_throwsForNullComment() {
        assertThrows(IllegalArgumentException.class,
                () -> photographService.addPhotographComment(1, null, "session1"));
    }

    @Test
    void addPhotographComment_throwsForCommentExceeding50Chars() {
        String longComment = "a".repeat(51);

        assertThrows(IllegalArgumentException.class,
                () -> photographService.addPhotographComment(1, longComment, "session1"));
    }

    @Test
    void addPhotographComment_succeedsWithValidComment() {
        User user = new User("testuser");
        when(userCertificateService.getUserLoginCertificate("session1")).thenReturn(user);
        when(photographMapper.selectPhotographByIdAndUsername(eq(1), eq("testuser")))
                .thenReturn(null);

        photographService.addPhotographComment(1, "nice photo", "session1");

        verify(photographMapper).insertPhotographComment(any(PhotographCommentEntity.class));
        verify(interactionNotificationService).createInteractionNotification(
                eq("photograph"), eq("comment"), isNull(), eq("testuser"),
                eq("1"), any(), eq("comment"), anyString(), anyString());
    }

    @Test
    void uploadPhotographItemPicture_logsErrorButDoesNotThrowOnR2Failure() {
        InputStream stream = new ByteArrayInputStream("fake".getBytes());
        doThrow(new RuntimeException("R2 unavailable"))
                .when(r2StorageService).uploadObject(anyString(), anyString(), any(InputStream.class));

        assertDoesNotThrow(() -> photographService.uploadPhotographItemPicture(1, 1, stream));
    }

    @Test
    void getPhotographById_throwsDataNotExistExceptionWhenNotFound() {
        User user = new User("testuser");
        when(userCertificateService.getUserLoginCertificate("session1")).thenReturn(user);
        when(photographMapper.selectPhotographByIdAndUsername(1, "testuser"))
                .thenReturn(Collections.emptyList());

        assertThrows(DataNotExistException.class,
                () -> photographService.getPhotographById(1, "session1"));
    }

    @Test
    void likePhotograph_insertsLikeForFirstTimeLiker() {
        User user = new User("testuser");
        when(userCertificateService.getUserLoginCertificate("session1")).thenReturn(user);
        when(photographMapper.selectPhotographLikeCountByPhotoIdAndUsername(1, "testuser"))
                .thenReturn(0);
        when(photographMapper.selectPhotographByIdAndUsername(eq(1), eq("testuser")))
                .thenReturn(null);

        photographService.LikePhotograph(1, "session1");

        verify(photographMapper).insertPhotographLike(1, "testuser");
        verify(interactionNotificationService).createInteractionNotification(
                eq("photograph"), eq("like"), isNull(), eq("testuser"),
                eq("1"), isNull(), eq("like"), anyString(), anyString());
    }
}
