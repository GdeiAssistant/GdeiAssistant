package cn.gdeiassistant.common.tools.SpringUtils;

import cn.gdeiassistant.common.exception.CommonException.FeatureNotEnabledException;
import cn.gdeiassistant.common.pojo.Config.R2Config;
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
        if (!isEnabled()) {
            return;
        }
        try {
            s3Client.deleteObject(DeleteObjectRequest.builder().bucket(resolveBucket(bucket)).key(key).build());
        } catch (Exception ignored) {
            // 忽略不存在等情况
        }
    }

    /**
     * 复制对象并删除源对象，适用于前端先上传临时对象、后端再归档到业务路径。
     */
    public void moveObject(String sourceKey, String targetKey) {
        moveObject(null, sourceKey, targetKey);
    }

    public void moveObject(String bucket, String sourceKey, String targetKey) {
        if (!isEnabled()) {
            throw new FeatureNotEnabledException("对象存储未开启，无法归档上传文件");
        }
        String resolvedBucket = resolveBucket(bucket);
        s3Client.copyObject(CopyObjectRequest.builder()
                .sourceBucket(resolvedBucket)
                .sourceKey(sourceKey)
                .destinationBucket(resolvedBucket)
                .destinationKey(targetKey)
                .build());
        s3Client.deleteObject(DeleteObjectRequest.builder()
                .bucket(resolvedBucket)
                .key(sourceKey)
                .build());
    }

    /**
     * 生成预签名 GET URL；若配置了 customDomain 则替换 host 为自定义域名。
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
        Duration duration = Duration.ofMillis(unit.toMillis(expire));
        GetObjectRequest getRequest = GetObjectRequest.builder().bucket(resolvedBucket).key(key).build();
        PresignedGetObjectRequest presigned = s3Presigner.presignGetObject(
                GetObjectPresignRequest.builder().signatureDuration(duration).getObjectRequest(getRequest).build());
        String url = presigned.url().toString().replace("http://", "https://");
        if (r2Config.getCustomDomain() != null && !r2Config.getCustomDomain().isEmpty()) {
            try {
                URL original = presigned.url();
                String path = original.getPath();
                if (original.getQuery() != null) {
                    path += "?" + original.getQuery();
                }
                String domain = r2Config.getCustomDomain().replaceFirst("^https?://", "").split("/")[0];
                url = "https://" + domain + path;
            } catch (Exception ignored) {
            }
        }
        return url;
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
        return presigned.url().toString().replace("http://", "https://");
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
