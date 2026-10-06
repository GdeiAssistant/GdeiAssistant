package cn.gdeiassistant.core.social.service;

import cn.gdeiassistant.common.tools.springutils.R2StorageService;
import cn.gdeiassistant.core.social.exception.SocialException;
import cn.gdeiassistant.core.social.image.ChatImageCodec;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SocialChatImageServiceTest {

    @Mock
    private R2StorageService r2StorageService;

    @InjectMocks
    private SocialChatImageService imageService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(imageService, "chatBucketName", "gdeiassistant-chat");
        ReflectionTestUtils.setField(imageService, "defaultBucketName", "gdeiassistant-userdata");
        when(r2StorageService.isEnabled()).thenReturn(true);
    }

    @Test
    void disabledWhenChatBucketMissingOrSameAsDefault() {
        ReflectionTestUtils.setField(imageService, "chatBucketName", "");
        assertFalse(imageService.isImageMessagingEnabled());

        ReflectionTestUtils.setField(imageService, "chatBucketName", "gdeiassistant-userdata");
        assertFalse(imageService.isImageMessagingEnabled());

        ReflectionTestUtils.setField(imageService, "chatBucketName", "gdeiassistant-chat");
        assertTrue(imageService.isImageMessagingEnabled());
    }

    @Test
    void uploadFailureMapsToImageUnavailableWithoutLeaking() {
        ChatImageCodec.CanonicalImage image = new ChatImageCodec.CanonicalImage(
                new byte[]{1}, ChatImageCodec.MIME_PNG, 1, 1, "abc");
        doThrow(new RuntimeException("https://secret.example/bucket")).when(r2StorageService)
                .uploadBytesStrict(anyString(), anyString(), any(), anyString());

        SocialException ex = assertThrows(SocialException.class,
                () -> imageService.uploadCanonical("chat/k", image));
        assertEquals("SOCIAL_IMAGE_UNAVAILABLE", ex.getErrorCode());
        assertFalse(ex.getMessage().contains("secret"));
        assertFalse(ex.getMessage().contains("https://"));
        verify(r2StorageService).deleteObjectStrict(eq("gdeiassistant-chat"), eq("chat/k"));
    }

    @Test
    void rollbackDeletesOnlyRegisteredKeyAndSurfacesDeleteFailure() {
        TransactionSynchronizationManager.initSynchronization();
        try {
            imageService.registerRollbackDelete("chat/only-this");
            assertEquals(1, TransactionSynchronizationManager.getSynchronizations().size());
            doThrow(new RuntimeException("delete-denied")).when(r2StorageService)
                    .deleteObjectStrict("gdeiassistant-chat", "chat/only-this");
            for (TransactionSynchronization sync : TransactionSynchronizationManager.getSynchronizations()) {
                sync.afterCompletion(TransactionSynchronization.STATUS_ROLLED_BACK);
            }
            verify(r2StorageService).deleteObjectStrict("gdeiassistant-chat", "chat/only-this");
        } finally {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }

    @Test
    void downloadFailureMapsToUnavailable() throws Exception {
        when(r2StorageService.downloadBytesBounded(eq("gdeiassistant-chat"), eq("chat/k"), anyInt()))
                .thenThrow(new RuntimeException("NoSuchBucket https://leak"));
        SocialException ex = assertThrows(SocialException.class, () -> imageService.downloadBounded("chat/k"));
        assertEquals("SOCIAL_IMAGE_UNAVAILABLE", ex.getErrorCode());
        assertFalse(ex.getMessage().contains("leak"));
    }
}
