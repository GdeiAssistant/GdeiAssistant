package cn.gdeiassistant.core.objectstorage.service;

import cn.gdeiassistant.common.tools.springutils.R2StorageService;
import cn.gdeiassistant.core.objectstorage.mapper.StoredAssetMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.transaction.TransactionDefinition;
import java.io.InputStream;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

/** Durable file lifecycle: pending records survive a failed business transaction. */
@Service
public class StoredAssetService {
    private final R2StorageService storage;
    private final StoredAssetMapper assets;
    private final TransactionTemplate independent;

    public StoredAssetService(R2StorageService storage, StoredAssetMapper assets,
                              @Qualifier("appTransactionManager") PlatformTransactionManager transactions) {
        this.storage = storage;
        this.assets = assets;
        independent = new TransactionTemplate(transactions);
        independent.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    }

    public void pending(String key, String owner, String type, long length) {
        independent.executeWithoutResult(status -> assets.pending(storage.getBucketName(), key, owner, type, length));
    }

    public void ready(String key) {
        if (assets.ready(storage.getBucketName(), key) != 1) {
            // An expired publication lost its lease: retain cleanup work and roll back its SQL row.
            pending(key, null, null, 0);
            throw new IllegalStateException("Upload expired before publication completed");
        }
    }

    public void uploadObject(String key, InputStream input) {
        if (input == null) throw new IllegalArgumentException("Missing upload");
        try {
            boolean voice = key.startsWith("secret/voice/");
            int limit = voice ? cn.gdeiassistant.common.constant.ValueConstantUtils.MAX_VOICE_SIZE : cn.gdeiassistant.common.constant.ValueConstantUtils.MAX_IMAGE_SIZE;
            byte[] bytes = input.readNBytes(limit + 1);
            if (bytes.length == 0 || bytes.length >= limit) throw new IllegalArgumentException("不合法的图片文件");
            String type = UploadService.validateMedia(bytes);
            if (type == null || !(voice ? type.startsWith("audio/") : type.startsWith("image/"))) throw new IllegalArgumentException("不合法的图片文件");
            pending(key, null, type, bytes.length);
            storage.uploadBytesStrict(null, key, bytes, type);
            ready(key);
        } catch (IOException e) { throw new IllegalStateException("Upload could not be read", e); }
    }

    public void deleteObject(String key) {
        independent.executeWithoutResult(status -> assets.deleting(storage.getBucketName(), key));
        independent.executeWithoutResult(status -> {
            var current = assets.lock(storage.getBucketName(), key);
            if (current == null || !"DELETING".equals(current.get("status"))) throw new IllegalStateException("Object changed during deletion");
            storage.deleteObjectStrict(null, key);
            assets.remove(storage.getBucketName(), key);
        });
    }

    public String generatePresignedUrl(String key, long expire, TimeUnit unit) {
        if (!storage.isEnabled()) return "";
        var record = assets.find(storage.getBucketName(), key);
        if (record != null && "READY".equals(record.get("status"))) return storage.generateKnownObjectUrl(null, key, expire, unit);
        if (record != null && !"MISSING".equals(record.get("status"))) return "";
        if (record != null && assets.expireMissing(storage.getBucketName(), key) == 0) return "";
        var metadata = storage.headObjectMetadata(key);
        assets.discovered(storage.getBucketName(), key, metadata == null ? "MISSING" : "READY",
                metadata == null ? null : metadata.contentType(), metadata == null ? 0 : metadata.contentLength());
        return metadata == null ? "" : storage.generateKnownObjectUrl(null, key, expire, unit);
    }

    public String firstReadyKey(String[] keys) {
        if (!storage.isEnabled() || keys.length == 0) return null;
        return assets.firstReadyKey(storage.getBucketName(), java.util.List.of(keys));
    }

    @Scheduled(fixedDelayString="${storage.cleanup-delay-ms:60000}")
    public void cleanAbandonedUploads() {
        if (!storage.isEnabled()) return;
        for (var item : assets.cleanupCandidates()) {
            String bucket = String.valueOf(item.get("bucket")), key = String.valueOf(item.get("object_key"));
            try {
                independent.executeWithoutResult(status -> {
                    // Lock and recheck the age. A new upload cannot be deleted by an older cleanup batch.
                    if (assets.lockAbandoned(bucket, key) == null) return;
                    storage.deleteObjectStrict(bucket, key);
                    assets.remove(bucket, key);
                });
            } catch (Exception failure) {
                // Retain the pending record for the next bounded batch; never pretend deletion succeeded.
                org.slf4j.LoggerFactory.getLogger(getClass()).warn("Object cleanup deferred ({})", failure.getClass().getSimpleName());
            }
        }
    }
}
