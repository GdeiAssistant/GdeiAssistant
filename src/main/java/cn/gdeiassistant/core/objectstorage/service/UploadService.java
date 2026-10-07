package cn.gdeiassistant.core.objectstorage.service;

import cn.gdeiassistant.common.tools.springutils.R2StorageService;
import cn.gdeiassistant.common.tools.utils.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class UploadService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM/dd");

    @Autowired
    private R2StorageService r2StorageService;

    @Autowired
    private cn.gdeiassistant.core.userlogin.service.UserCertificateService certificates;
    @Autowired
    private cn.gdeiassistant.common.tools.springutils.RedisDaoUtils redis;
    @Autowired
    private StoredAssetService assets;

    private String owner(String sessionId) {
        var user = certificates.getUserLoginCertificate(sessionId);
        if (user == null) throw new SecurityException("Missing upload owner");
        return cn.gdeiassistant.common.tools.utils.StringEncryptUtils.sha256HexString(user.getUsername());
    }

    public Map<String, String> createPresignedUpload(String sessionId, String fileName, String contentType) {
        String normalizedContentType = contentType == null ? null : contentType.trim();
        String objectKey = buildObjectKey(owner(sessionId), fileName);
        String presignedUrl = r2StorageService.generatePutPresignedUrl(objectKey, normalizedContentType, 15, TimeUnit.MINUTES);
        redis.set("upload-ticket:" + objectKey, normalizedContentType, 20, TimeUnit.MINUTES);
        assets.pending(objectKey, owner(sessionId), normalizedContentType, 0);
        Map<String, String> result = new LinkedHashMap<>(2);
        result.put("url", presignedUrl);
        result.put("objectKey", objectKey);
        return result;
    }

    public void moveUpload(String sessionId, String sourceKey, String targetKey) {
        String owner = owner(sessionId);
        if (sourceKey == null || !sourceKey.startsWith("upload/" + owner + "/") || sourceKey.contains("..")) throw new SecurityException("Upload belongs to another user");
        String type = redis.get("upload-ticket:" + sourceKey);
        if (type == null) throw new IllegalArgumentException("Upload has expired or was already submitted");
        boolean voice = targetKey.startsWith("secret/voice/");
        if (!(voice ? type.startsWith("audio/") : type.startsWith("image/"))) throw new IllegalArgumentException("Upload has the wrong media type");
        var metadata = r2StorageService.headObjectMetadata(sourceKey);
        int limit = voice ? cn.gdeiassistant.common.constant.ValueConstantUtils.MAX_VOICE_SIZE : cn.gdeiassistant.common.constant.ValueConstantUtils.MAX_IMAGE_SIZE;
        if (metadata == null || metadata.contentLength() == null || metadata.contentLength() <= 0 || metadata.contentLength() >= limit || metadata.eTag() == null || metadata.eTag().isBlank()
                || !type.equals(metadata.contentType())) throw new IllegalArgumentException("Upload metadata does not match");
        try {
            byte[] bytes = r2StorageService.downloadBytesBounded(null, sourceKey, limit);
            if (bytes == null || !type.equals(validateMedia(bytes))) throw new IllegalArgumentException("Upload content does not match its declared type");
        } catch (java.io.IOException e) { throw new IllegalArgumentException("Invalid upload", e); }
        if (!redis.compareAndDelete("upload-ticket:" + sourceKey, type)) throw new IllegalArgumentException("Upload was already submitted");
        assets.pending(targetKey, owner, type, metadata.contentLength());
        r2StorageService.moveVerifiedObject(sourceKey, targetKey, metadata.eTag());
        assets.ready(targetKey);
    }

    static String validateMedia(byte[] bytes) {
        String type = detectMediaType(bytes);
        if (type == null || !type.startsWith("image/") || type.equals("image/webp")) return type;
        try (var input = new javax.imageio.stream.MemoryCacheImageInputStream(new java.io.ByteArrayInputStream(bytes))) {
            var readers = javax.imageio.ImageIO.getImageReaders(input);
            if (!readers.hasNext()) return null;
            var reader = readers.next();
            try {
                reader.setInput(input);
                int width = reader.getWidth(0), height = reader.getHeight(0);
                if (width <= 0 || height <= 0 || (long) width * height > 16_000_000) return null;
                return reader.read(0) == null ? null : type;
            } finally { reader.dispose(); }
        } catch (java.io.IOException | RuntimeException invalid) { return null; }
    }

    /** Detect file signatures rather than trusting browser MIME or suffix. */
    static String detectMediaType(byte[] bytes) {
        if (bytes == null || bytes.length < 12) return null;
        int a=bytes[0]&255, b=bytes[1]&255;
        String first = new String(bytes, 0, 4, java.nio.charset.StandardCharsets.ISO_8859_1);
        String middle = new String(bytes, 8, 4, java.nio.charset.StandardCharsets.ISO_8859_1);
        if (a==255 && b==216 && (bytes[2]&255)==255) return "image/jpeg";
        if (a==137 && new String(bytes,1,3,java.nio.charset.StandardCharsets.ISO_8859_1).equals("PNG")) return "image/png";
        if (first.equals("GIF8")) return "image/gif";
        if (first.equals("RIFF") && middle.equals("WEBP")) return "image/webp";
        if (a==66 && b==77) return "image/bmp";
        if (first.equals("RIFF") && middle.equals("WAVE")) return "audio/wav";
        if (a == 0x1a && b == 0x45 && (bytes[2]&255) == 0xdf && (bytes[3]&255) == 0xa3) {
            String container = new String(bytes, java.nio.charset.StandardCharsets.ISO_8859_1);
            if (container.substring(0, Math.min(container.length(), 128)).contains("webm")
                    && (container.contains("A_OPUS") || container.contains("A_VORBIS"))) return "audio/webm";
            return null;
        }
        if (first.equals("OggS")) return "audio/ogg";
        if (new String(bytes,0,3,java.nio.charset.StandardCharsets.ISO_8859_1).equals("ID3")) return "audio/mpeg";
        if (a==255 && (b&0xF6)==0xF0) return "audio/aac";
        if (a==255 && (b&0xE0)==0xE0) return "audio/mpeg";
        if (new String(bytes,4,4,java.nio.charset.StandardCharsets.ISO_8859_1).equals("ftyp")) return "audio/mp4";
        return null;
    }

    private String buildObjectKey(String owner, String fileName) {
        String extension = extractExtension(fileName);
        return "upload/" + owner + "/" + DATE_FORMATTER.format(LocalDate.now()) + "/"
                + UUID.randomUUID().toString().replace("-", "") + extension;
    }

    private String extractExtension(String fileName) {
        if (StringUtils.isBlank(fileName)) {
            return "";
        }
        String normalizedFileName = fileName.replace("\\", "/");
        int slashIndex = normalizedFileName.lastIndexOf('/');
        String baseName = slashIndex >= 0 ? normalizedFileName.substring(slashIndex + 1) : normalizedFileName;
        int dotIndex = baseName.lastIndexOf('.');
        if (dotIndex < 0 || dotIndex == baseName.length() - 1) {
            return "";
        }
        String extension = baseName.substring(dotIndex).toLowerCase();
        return extension.matches("\\.[a-z0-9]{1,16}") ? extension : "";
    }
}
