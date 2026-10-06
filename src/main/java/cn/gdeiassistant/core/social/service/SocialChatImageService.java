package cn.gdeiassistant.core.social.service;

import cn.gdeiassistant.common.tools.SpringUtils.R2StorageService;
import cn.gdeiassistant.core.social.exception.SocialException;
import cn.gdeiassistant.core.social.image.ChatImageCodec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.io.IOException;
import java.util.UUID;

/**
 * 私信图片存储：独立 r2.chatBucketName 私有 bucket；缺配置或与默认 bucket 同名则禁用。
 * 真实 privateACL 无法在本地证明，启用前须人工核实 bucket 无私有公开读取。
 */
@Service
public class SocialChatImageService {

    private static final Logger log = LoggerFactory.getLogger(SocialChatImageService.class);

    @Autowired
    private R2StorageService r2StorageService;

    @Value("${r2.chatBucketName:}")
    private String chatBucketName;

    @Value("${r2.bucketName:}")
    private String defaultBucketName;

    public boolean isImageMessagingEnabled() {
        String chat = normalize(chatBucketName);
        String defaults = normalize(defaultBucketName);
        if (chat == null) {
            return false;
        }
        if (defaults != null && chat.equalsIgnoreCase(defaults)) {
            return false;
        }
        return r2StorageService.isEnabled();
    }

    public String requireChatBucket() {
        if (!isImageMessagingEnabled()) {
            throw SocialException.imageUnavailable();
        }
        return normalize(chatBucketName);
    }

    public String newObjectKey() {
        return "chat/" + UUID.randomUUID() + "/" + UUID.randomUUID();
    }

    public void uploadCanonical(String key, ChatImageCodec.CanonicalImage image) {
        String bucket = requireChatBucket();
        try {
            r2StorageService.uploadBytesStrict(bucket, key, image.bytes, image.contentType);
        } catch (SocialException ex) {
            throw ex;
        } catch (Exception ex) {
            safeDeleteBestEffort(bucket, key, "upload-failed");
            throw SocialException.imageUnavailable();
        }
    }

    /**
     * 注册事务 rollback 时仅删除本次 key；删除失败留下无秘密诊断，不伪装成功。
     */
    public void registerRollbackDelete(String key) {
        String bucket = requireChatBucket();
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCompletion(int status) {
                if (status != STATUS_ROLLED_BACK) {
                    return;
                }
                try {
                    r2StorageService.deleteObjectStrict(bucket, key);
                } catch (Exception ex) {
                    log.warn("chat-image rollback delete failed bucketConfigured=true keyPresent=true reason={}",
                            sanitize(ex));
                }
            }
        });
    }

    public byte[] downloadBounded(String key) {
        String bucket = requireChatBucket();
        try {
            byte[] bytes = r2StorageService.downloadBytesBounded(bucket, key, ChatImageCodec.MAX_BYTES);
            return bytes;
        } catch (SocialException ex) {
            throw ex;
        } catch (IOException ex) {
            if (ex.getMessage() != null && ex.getMessage().contains("exceeds maxBytes")) {
                throw SocialException.imageUnavailable();
            }
            throw SocialException.imageUnavailable();
        } catch (Exception ex) {
            throw SocialException.imageUnavailable();
        }
    }

    public void deleteStrictOrDiagnose(String key, String reason) {
        String bucket = normalize(chatBucketName);
        if (bucket == null) {
            log.warn("chat-image delete skipped: chat bucket not configured reason={}", reason);
            return;
        }
        try {
            r2StorageService.deleteObjectStrict(bucket, key);
        } catch (Exception ex) {
            log.warn("chat-image delete failed bucketConfigured=true keyPresent=true reason={} detail={}",
                    reason, sanitize(ex));
            throw SocialException.imageUnavailable();
        }
    }

    private void safeDeleteBestEffort(String bucket, String key, String reason) {
        try {
            r2StorageService.deleteObjectStrict(bucket, key);
        } catch (Exception ex) {
            log.warn("chat-image cleanup after upload failure failed keyPresent=true reason={} detail={}",
                    reason, sanitize(ex));
        }
    }

    private static String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private static String sanitize(Exception ex) {
        String name = ex.getClass().getSimpleName();
        String message = ex.getMessage();
        if (message == null || message.isBlank()) {
            return name;
        }
        String lower = message.toLowerCase();
        if (lower.contains("http://") || lower.contains("https://") || lower.contains("bucket")
                || lower.contains("amazonaws") || lower.contains("r2.cloudflare")) {
            return name + ":redacted";
        }
        return name + ":" + message.replaceAll("[\\r\\n\\t]", " ");
    }
}
