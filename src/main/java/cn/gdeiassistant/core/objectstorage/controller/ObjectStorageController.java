package cn.gdeiassistant.core.objectStorage.controller;

import cn.gdeiassistant.common.annotation.RateLimit;
import cn.gdeiassistant.common.annotation.RestAuthentication;
import cn.gdeiassistant.common.pojo.Result.DataJsonResult;
import cn.gdeiassistant.common.pojo.Result.JsonResult;
import cn.gdeiassistant.common.tools.Utils.StringUtils;
import cn.gdeiassistant.core.objectStorage.service.UploadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.Set;

@RestController
public class ObjectStorageController {

    /**
     * 允许上传的 MIME 类型白名单（图片 + 音频）
     */
    private static final Set<String> ALLOWED_MIME_TYPES = Set.of(
            "image/jpeg", "image/png", "image/gif", "image/webp", "image/bmp",
            "audio/mpeg", "audio/wav", "audio/ogg", "audio/aac", "audio/mp4"
    );

    /**
     * 文件扩展名到 MIME 类型的映射，用于验证扩展名与 contentType 一致性
     */
    private static final Map<String, Set<String>> EXTENSION_MIME_MAP = Map.ofEntries(
            Map.entry(".jpg", Set.of("image/jpeg")),
            Map.entry(".jpeg", Set.of("image/jpeg")),
            Map.entry(".png", Set.of("image/png")),
            Map.entry(".gif", Set.of("image/gif")),
            Map.entry(".webp", Set.of("image/webp")),
            Map.entry(".bmp", Set.of("image/bmp")),
            Map.entry(".mp3", Set.of("audio/mpeg")),
            Map.entry(".wav", Set.of("audio/wav")),
            Map.entry(".ogg", Set.of("audio/ogg")),
            Map.entry(".aac", Set.of("audio/aac")),
            Map.entry(".m4a", Set.of("audio/mp4"))
    );

    @Autowired
    private UploadService uploadService;

    /**
     * 生成前端直传 Cloudflare R2 的预签名 PUT URL。
     */
    @RequestMapping(value = "/api/upload/presignedUrl", method = RequestMethod.GET)
    @RestAuthentication
    @RateLimit(maxRequests = 20, windowSeconds = 60)
    public DataJsonResult<Map<String, String>> getPresignedUploadUrl(HttpServletRequest request,
                                                                     @RequestParam("fileName") String fileName,
                                                                     @RequestParam("contentType") String contentType) {
        if (StringUtils.isBlank(fileName)) {
            return new DataJsonResult<>(new JsonResult(false, "fileName 不能为空"));
        }
        if (StringUtils.isBlank(contentType) || !contentType.contains("/")) {
            return new DataJsonResult<>(new JsonResult(false, "contentType 不合法"));
        }

        String normalizedType = contentType.trim().toLowerCase();
        if (!ALLOWED_MIME_TYPES.contains(normalizedType)) {
            return new DataJsonResult<>(new JsonResult(false, "不支持的文件类型: " + contentType));
        }

        String extension = extractExtension(fileName);
        if (!extension.isEmpty()) {
            Set<String> validMimes = EXTENSION_MIME_MAP.get(extension);
            if (validMimes != null && !validMimes.contains(normalizedType)) {
                return new DataJsonResult<>(new JsonResult(false, "文件扩展名与 contentType 不匹配"));
            }
        }

        return new DataJsonResult<>(true, uploadService.createPresignedUpload(fileName, contentType));
    }

    private static String extractExtension(String fileName) {
        if (fileName == null) return "";
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex < 0 || dotIndex == fileName.length() - 1) return "";
        return fileName.substring(dotIndex).toLowerCase();
    }

}
