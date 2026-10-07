package cn.gdeiassistant.common.tools.springutils;

import cn.gdeiassistant.common.exception.commonexception.FeatureNotEnabledException;
import cn.gdeiassistant.common.pojo.config.R2Config;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * Cloudflare R2 对象存储服务（S3 兼容 API）。
 * 提供上传、下载、删除与预签名 URL，供业务模块直接使用。
 */
@Component
public class R2StorageService {

    @Autowired
    private R2Config r2Config;

    @Autowired(required = false)
    private S3Client s3Client;

    @Autowired(required = false)
    private S3Presigner s3Presigner;

    public boolean isEnabled() {
        return r2Config.isEnabled() && s3Client != null && s3Presigner != null;
    }

    public String getBucketName() {
        return r2Config.getBucketName();
    }

    /**
     * 上传对象（根据 key 后缀设置 Content-Type）；未配置 R2 时抛出 FeatureNotEnabledException。
     */
    public void uploadObject(String key, InputStream inputStream) {
        uploadObject(null, key, inputStream);
    }

    public void uploadObject(String bucket, String key, InputStream inputStream) {
        if (inputStream == null) {
            return;
        }
        if (!isEnabled()) {
            throw new FeatureNotEnabledException("对象存储未开启，无法上传图片");
        }
        byte[] bytes;
        try {
            ByteArrayOutputStream buf = new ByteArrayOutputStream();
            byte[] b = new byte[8192];
            int n;
            while ((n = inputStream.read(b)) != -1) {
                buf.write(b, 0, n);
            }
            bytes = buf.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("R2 upload failed: cannot read stream for " + key, e);
        }
        String contentType = contentTypeFromKey(key);
        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(resolveBucket(bucket))
                .key(key)
                .contentType(contentType)
                .contentLength((long) bytes.length)
                .build();
        s3Client.putObject(request, RequestBody.fromBytes(bytes));
    }

    /**
     * 下载对象，不存在返回 null
     */
    public InputStream downloadObject(String key) {
        return downloadObject(null, key);
    }

    public InputStream downloadObject(String bucket, String key) {
        if (!isEnabled()) {
            return null;
        }
        try {
            return s3Client.getObject(GetObjectRequest.builder().bucket(resolveBucket(bucket)).key(key).build());
        } catch (NoSuchKeyException e) {
            return null;
        }
    }

    /**
     * 删除对象（存在则删）
     */
    public void deleteObject(String key) {
        deleteObject(null, key);
    }

    public void deleteObject(String bucket, String key) {
        deleteObjectStrict(bucket, key);
    }

    /**
     * 私信图片专用：上传到指定 bucket，显式 Content-Type；失败抛出且不吞异常。
     */
    public void uploadBytesStrict(String bucket, String key, byte[] bytes, String contentType) {
        if (!isEnabled()) {
            throw new FeatureNotEnabledException("对象存储未开启，无法上传图片");
        }
        if (bytes == null) {
            throw new IllegalArgumentException("bytes 不能为空");
        }
        if (key == null || key.isBlank()) {
            throw new IllegalArgumentException("key 不能为空");
        }
        String resolvedBucket = resolveBucket(bucket);
        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(resolvedBucket)
                .key(key)
                .contentType(contentType == null || contentType.isBlank() ? "application/octet-stream" : contentType)
                .contentLength((long) bytes.length)
                .build();
        s3Client.putObject(request, RequestBody.fromBytes(bytes));
    }

    /**
     * 私信图片专用：严格删除指定对象；失败向上抛出，不伪装成功。
     */
    public void deleteObjectStrict(String bucket, String key) {
        if (!isEnabled()) {
            throw new FeatureNotEnabledException("对象存储未开启，无法删除图片");
        }
        if (key == null || key.isBlank()) {
            throw new IllegalArgumentException("key 不能为空");
        }
        try {
            s3Client.deleteObject(DeleteObjectRequest.builder().bucket(resolveBucket(bucket)).key(key).build());
        } catch (NoSuchKeyException ignored) {
            // Already removed; service/permission failures still propagate to the retry worker.
        }
    }

    /**
     * 私信图片专用：有界下载，超过 maxBytes 拒绝；不存在返回 null。
     */
    public byte[] downloadBytesBounded(String bucket, String key, int maxBytes) throws IOException {
        if (!isEnabled()) {
            return null;
        }
        if (key == null || key.isBlank()) {
            return null;
        }
        if (maxBytes <= 0) {
            throw new IllegalArgumentException("maxBytes 必须为正");
        }
        try (InputStream in = s3Client.getObject(GetObjectRequest.builder()
                .bucket(resolveBucket(bucket))
                .key(key)
                .build())) {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            byte[] chunk = new byte[8192];
            int total = 0;
            int n;
            while ((n = in.read(chunk)) != -1) {
                total += n;
                if (total > maxBytes) {
                    throw new IOException("object exceeds maxBytes");
                }
                buffer.write(chunk, 0, n);
            }
            return buffer.toByteArray();
        } catch (NoSuchKeyException e) {
            return null;
        }
    }

    private static final String TEMP_UPLOAD_PREFIX = "upload/";

    /**
     * 校验 sourceKey 是否为合法的临时上传路径。
     * 仅允许以 "upload/" 开头且不含路径穿越字符的 key，防止攻击者通过
     * 客户端提交任意 objectKey 来移动或删除其他用户的对象。
     */
    private void validateSourceKey(String sourceKey) {
        if (sourceKey == null || sourceKey.isEmpty()) {
            throw new IllegalArgumentException("sourceKey 不能为空");
        }
        if (sourceKey.contains("..")) {
            throw new IllegalArgumentException("sourceKey 包含非法路径穿越字符: " + sourceKey);
        }
        if (!sourceKey.startsWith(TEMP_UPLOAD_PREFIX)) {
            throw new IllegalArgumentException("sourceKey 必须位于临时上传目录 (upload/): " + sourceKey);
        }
    }

    /**
     * 复制对象并删除源对象，适用于前端先上传临时对象、后端再归档到业务路径。
     */
    /**
     * 生成私有对象 GET URL；保留签名中的 S3 host，不替换自定义域名。
     * 对象不存在时返回空字符串（与原有 OSS 行为一致）。
     */
    public String generatePresignedUrl(String key, long expire, TimeUnit unit) {
        return generatePresignedUrl(null, key, expire, unit);
    }

    public String generatePresignedUrl(String bucket, String key, long expire, TimeUnit unit) {
        if (!isEnabled()) {
            return "";
        }
        String resolvedBucket = resolveBucket(bucket);
        try {
            s3Client.headObject(HeadObjectRequest.builder().bucket(resolvedBucket).key(key).build());
        } catch (NoSuchKeyException e) {
            return "";
        } catch (Exception e) {
            return "";
        }
        return generateKnownObjectUrl(bucket, key, expire, unit);
    }

    /** Sign confirmed metadata locally, without a remote existence probe. */
    public String generateKnownObjectUrl(String bucket, String key, long expire, TimeUnit unit) {
        if (!isEnabled()) return "";
        var request = GetObjectRequest.builder().bucket(resolveBucket(bucket)).key(key).build();
        return s3Presigner.presignGetObject(GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMillis(unit.toMillis(expire))).getObjectRequest(request).build()).url().toString();
    }

    public HeadObjectResponse headObjectMetadata(String key) {
        if (!isEnabled()) throw new FeatureNotEnabledException("对象存储未开启");
        try { return s3Client.headObject(HeadObjectRequest.builder().bucket(resolveBucket(null)).key(key).build()); }
        catch (NoSuchKeyException missing) { return null; }
    }

    public void moveVerifiedObject(String sourceKey, String targetKey, String etag) {
        validateSourceKey(sourceKey);
        if (etag == null || etag.isBlank()) throw new IllegalArgumentException("Missing upload version");
        if (!isEnabled()) throw new FeatureNotEnabledException("对象存储未开启");
        s3Client.copyObject(CopyObjectRequest.builder().sourceBucket(resolveBucket(null)).sourceKey(sourceKey)
                .destinationBucket(resolveBucket(null)).destinationKey(targetKey).copySourceIfMatch(etag).build());
        deleteObjectStrict(null, sourceKey);
    }

    /**
     * 生成预签名 PUT URL，供前端直传到 Cloudflare R2。
     */
    public String generatePutPresignedUrl(String key, String contentType, long expire, TimeUnit unit) {
        return generatePutPresignedUrl(null, key, contentType, expire, unit);
    }

    public String generatePutPresignedUrl(String bucket, String key, String contentType, long expire, TimeUnit unit) {
        if (!isEnabled()) {
            throw new FeatureNotEnabledException("对象存储未开启，无法生成上传地址");
        }
        Duration duration = Duration.ofMillis(unit.toMillis(expire));
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(resolveBucket(bucket))
                .key(key)
                .contentType(contentType)
                .build();
        PresignedPutObjectRequest presigned = s3Presigner.presignPutObject(
                PutObjectPresignRequest.builder()
                        .signatureDuration(duration)
                        .putObjectRequest(putObjectRequest)
                        .build());
        return normalizePresignedUrl(presigned.url().toString());
    }

    private String normalizePresignedUrl(String url) {
        String endpoint = r2Config.getEffectivePresignEndpoint();
        if (endpoint != null && !endpoint.isBlank() && endpoint.startsWith("http://")) {
            return url;
        }
        return url.replace("http://", "https://");
    }

    private static String contentTypeFromKey(String key) {
        if (key == null) return "application/octet-stream";
        String lower = key.toLowerCase();
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) return "image/jpeg";
        if (lower.endsWith(".png")) return "image/png";
        if (lower.endsWith(".gif")) return "image/gif";
        if (lower.endsWith(".mp3")) return "audio/mpeg";
        if (lower.endsWith(".zip")) return "application/zip";
        return "application/octet-stream";
    }

    private String resolveBucket(String bucket) {
        String resolvedBucket = bucket;
        if (resolvedBucket == null || resolvedBucket.trim().isEmpty()) {
            resolvedBucket = r2Config.getBucketName();
        }
        if (resolvedBucket == null || resolvedBucket.trim().isEmpty()) {
            throw new FeatureNotEnabledException("对象存储未配置 bucketName");
        }
        return resolvedBucket;
    }

}
