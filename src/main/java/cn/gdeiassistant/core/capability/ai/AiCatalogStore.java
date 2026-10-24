package cn.gdeiassistant.core.capability.ai;

import com.alibaba.fastjson2.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.*;

/** Preserve observed retirements across restarts using the application's existing Redis. */
@Component
public class AiCatalogStore {
    private static final Logger log = LoggerFactory.getLogger(AiCatalogStore.class);
    private final StringRedisTemplate redis;
    public AiCatalogStore(StringRedisTemplate redis) { this.redis = redis; }
    public Optional<AiModelCatalog.Snapshot> load(AiProperties.Endpoint endpoint) {
        try {
            String value = redis.opsForValue().get(key(endpoint));
            if (value == null) return Optional.empty();
            JSONObject json = JSONObject.parseObject(value);
            return Optional.of(new AiModelCatalog.Snapshot(new HashSet<>(json.getList("models", String.class)),
                    new HashSet<>(json.getList("unavailableModels", String.class)), Instant.parse(json.getString("updatedAt")), false));
        } catch (Exception failure) {
            log.warn("AI catalog cache read failed for {} ({})", endpoint.getId(), failure.getClass().getSimpleName());
            return Optional.empty();
        }
    }
    public void save(AiProperties.Endpoint endpoint, AiModelCatalog.Snapshot snapshot) {
        try {
            redis.opsForValue().set(key(endpoint), JSONObject.of("models", snapshot.models(),
                    "unavailableModels", snapshot.unavailableModels(), "updatedAt", snapshot.updatedAt().toString()).toJSONString());
        } catch (Exception failure) {
            log.warn("AI catalog cache write failed for {} ({})", endpoint.getId(), failure.getClass().getSimpleName());
        }
    }
    private String key(AiProperties.Endpoint endpoint) throws java.security.NoSuchAlgorithmException {
        String identity = endpoint.getRegion() + "|" + endpoint.getProtocol() + "|" + endpoint.getBaseUrl() + "|" + endpoint.getCatalogPath();
        String digest = HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(identity.getBytes(StandardCharsets.UTF_8)));
        return "ai:catalog:v1:" + endpoint.getId() + ":" + digest;
    }
}
