package cn.gdeiassistant.core.secret.service;

import cn.gdeiassistant.common.pojo.Entity.User;
import cn.gdeiassistant.core.secret.mapper.SecretMapper;
import cn.gdeiassistant.core.secret.pojo.entity.SecretContentEntity;
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
class SecretServiceTest {

    @Mock
    private SecretMapper secretMapper;

    @Mock
    private UserCertificateService userCertificateService;

    @Mock
    private R2StorageService r2StorageService;

    @Mock
    private InteractionNotificationService interactionNotificationService;

    @InjectMocks
    private SecretService secretService;

    @Test
    void addSecretCommentThrowsIllegalArgumentExceptionForEmptyComment() {
        assertThrows(IllegalArgumentException.class,
                () -> secretService.addSecretComment(1, "session1", ""));
    }

    @Test
    void addSecretCommentThrowsIllegalArgumentExceptionForCommentOver200Chars() {
        String longComment = "a".repeat(201);

        assertThrows(IllegalArgumentException.class,
                () -> secretService.addSecretComment(1, "session1", longComment));
    }

    @Test
    void uploadVoiceSecretThrowsRuntimeExceptionOnR2Failure() {
        doThrow(new RuntimeException("R2 down"))
                .when(r2StorageService).uploadObject(eq("gdeiassistant-userdata"), eq("secret/voice/1.mp3"), any(InputStream.class));

        InputStream stream = new ByteArrayInputStream(new byte[]{1, 2, 3});

        assertThrows(RuntimeException.class,
                () -> secretService.uploadVoiceSecret(1, stream));
    }

    @Test
    void changeUserLikeStateInsertsLikeWhenLikeTrueAndNoExistingLike() throws Exception {
        User user = new User("testuser");
        when(userCertificateService.getUserLoginCertificate("session1")).thenReturn(user);
        when(secretMapper.selectSecretLike(1, "testuser")).thenReturn(0);
        SecretContentEntity content = new SecretContentEntity();
        content.setId(1);
        content.setUsername("poster");
        when(secretMapper.selectSecretByID(1)).thenReturn(content);

        secretService.changeUserLikeState(true, 1, "session1");

        verify(secretMapper).insertSecretLike(1, "testuser");
        verify(interactionNotificationService).createInteractionNotification(
                eq("secret"), eq("like"), eq("poster"), eq("testuser"),
                eq("1"), isNull(), eq("like"), anyString(), anyString()
        );
    }

    @Test
    void changeUserLikeStateDeletesLikeWhenLikeFalseAndExistingLike() throws Exception {
        User user = new User("testuser");
        when(userCertificateService.getUserLoginCertificate("session1")).thenReturn(user);

        secretService.changeUserLikeState(false, 1, "session1");

        verify(secretMapper).deleteSecretLike(1, "testuser");
        verify(secretMapper, never()).insertSecretLike(anyInt(), anyString());
    }
}
