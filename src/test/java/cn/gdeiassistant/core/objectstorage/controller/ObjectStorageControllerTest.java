package cn.gdeiassistant.core.objectStorage.controller;

import cn.gdeiassistant.common.pojo.Result.DataJsonResult;
import cn.gdeiassistant.core.objectStorage.service.UploadService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ObjectStorageControllerTest {

    @Mock
    private UploadService uploadService;

    @InjectMocks
    private ObjectStorageController controller;

    private MockHttpServletRequest request;

    @BeforeEach
    void setUp() {
        request = new MockHttpServletRequest();
        request.setAttribute("sessionId", "test-session");
    }

    @Test
    void shouldAcceptAllowedImageMimeType() {
        when(uploadService.createPresignedUpload("photo.jpg", "image/jpeg"))
                .thenReturn(Map.of("url", "https://r2.example.com/signed", "objectKey", "upload/photo.jpg"));

        DataJsonResult<Map<String, String>> result = controller.getPresignedUploadUrl(request, "photo.jpg", "image/jpeg");

        assertTrue(result.isSuccess());
        assertNotNull(result.getData());
        assertEquals("https://r2.example.com/signed", result.getData().get("url"));
    }

    @Test
    void shouldAcceptAllowedAudioMimeType() {
        when(uploadService.createPresignedUpload("voice.mp3", "audio/mpeg"))
                .thenReturn(Map.of("url", "https://r2.example.com/signed", "objectKey", "upload/voice.mp3"));

        DataJsonResult<Map<String, String>> result = controller.getPresignedUploadUrl(request, "voice.mp3", "audio/mpeg");

        assertTrue(result.isSuccess());
    }

    @Test
    void shouldRejectDisallowedMimeType() {
        DataJsonResult<Map<String, String>> result = controller.getPresignedUploadUrl(request, "exploit.exe", "application/octet-stream");

        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("不支持的文件类型"));
        verify(uploadService, never()).createPresignedUpload(anyString(), anyString());
    }

    @Test
    void shouldRejectHtmlMimeType() {
        DataJsonResult<Map<String, String>> result = controller.getPresignedUploadUrl(request, "page.html", "text/html");

        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("不支持的文件类型"));
    }

    @Test
    void shouldRejectMismatchedExtensionAndMimeType() {
        // .png 扩展名但声明 image/jpeg → 不匹配
        DataJsonResult<Map<String, String>> result = controller.getPresignedUploadUrl(request, "image.png", "image/jpeg");

        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("扩展名与 contentType 不匹配"));
    }

    @Test
    void shouldAcceptMatchingExtensionAndMimeType() {
        when(uploadService.createPresignedUpload("image.png", "image/png"))
                .thenReturn(Map.of("url", "https://r2.example.com/signed", "objectKey", "upload/image.png"));

        DataJsonResult<Map<String, String>> result = controller.getPresignedUploadUrl(request, "image.png", "image/png");

        assertTrue(result.isSuccess());
    }

    @Test
    void shouldRejectBlankFileName() {
        DataJsonResult<Map<String, String>> result = controller.getPresignedUploadUrl(request, "", "image/png");

        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("fileName 不能为空"));
    }

    @Test
    void shouldRejectBlankContentType() {
        DataJsonResult<Map<String, String>> result = controller.getPresignedUploadUrl(request, "photo.jpg", "");

        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("contentType 不合法"));
    }

    @Test
    void shouldRejectContentTypeWithoutSlash() {
        DataJsonResult<Map<String, String>> result = controller.getPresignedUploadUrl(request, "photo.jpg", "jpeg");

        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("contentType 不合法"));
    }

    @Test
    void shouldHandleCaseInsensitiveMimeType() {
        when(uploadService.createPresignedUpload("photo.jpg", "Image/JPEG"))
                .thenReturn(Map.of("url", "https://r2.example.com/signed", "objectKey", "upload/photo.jpg"));

        DataJsonResult<Map<String, String>> result = controller.getPresignedUploadUrl(request, "photo.jpg", "Image/JPEG");

        assertTrue(result.isSuccess());
    }

    @Test
    void shouldAllowUnknownExtensionWithValidMime() {
        // 无扩展名的文件，只要 MIME 在白名单就应允许
        when(uploadService.createPresignedUpload("noext", "image/png"))
                .thenReturn(Map.of("url", "https://r2.example.com/signed", "objectKey", "upload/noext"));

        DataJsonResult<Map<String, String>> result = controller.getPresignedUploadUrl(request, "noext", "image/png");

        assertTrue(result.isSuccess());
    }
}
