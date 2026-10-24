package cn.gdeiassistant.core.capability.ai;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/** Discovery only: remote metadata never grants permission to send images or change models. */
@Component
@org.springframework.boot.actuate.endpoint.annotation.Endpoint(id = "aimodels")
public class AiModelCatalog {
    private static final Logger log = LoggerFactory.getLogger(AiModelCatalog.class);
    public record Snapshot(Set<String> models, Set<String> unavailableModels, Instant updatedAt, boolean lastRefreshSucceeded) {
        public Snapshot { models = Set.copyOf(models); unavailableModels = Set.copyOf(unavailableModels); }
    }
    private record Listing(Set<String> models, Set<String> unavailable) {
    }
    private final AiProperties settings;
    private final RestTemplate http;
    private final AiCatalogStore store;
    private final Map<String, Snapshot> snapshots = new ConcurrentHashMap<>();
    public AiModelCatalog(AiProperties settings, AiProviderRegistry registry) { this(settings, registry, null); }
    @org.springframework.beans.factory.annotation.Autowired
    public AiModelCatalog(AiProperties settings, AiProviderRegistry registry, AiCatalogStore store) {
        this.settings = settings;
        this.http = registry.http();
        this.store = store;
    }
    @jakarta.annotation.PostConstruct
    public void restore() {
        if (store == null || !settings.isCatalogEnabled()) return;
        settings.getEndpoints().stream().filter(settings::permits).filter(AiProperties.Endpoint::isConfigured)
                .forEach(endpoint -> store.load(endpoint).ifPresent(snapshot -> snapshots.put(endpoint.getId(), snapshot)));
    }
    @org.springframework.boot.actuate.endpoint.annotation.ReadOperation
    public Map<String, Snapshot> snapshots() { return Map.copyOf(snapshots); }

    @Scheduled(initialDelayString = "${api.ai.catalog-initial-delay-ms:30000}",
            fixedDelayString = "${api.ai.catalog-refresh-ms:21600000}")
    public void refresh() {
        if (!settings.isCatalogEnabled()) return;
        for (AiProperties.Endpoint endpoint : settings.getEndpoints()) {
            if (!settings.permits(endpoint) || !endpoint.isConfigured()) continue;
            try {
                Listing listing = fetch(endpoint);
                Snapshot updated = snapshots.compute(endpoint.getId(), (id, old) -> {
                    Set<String> unavailable = new HashSet<>(listing.unavailable());
                    if (old != null) {
                        unavailable.addAll(old.unavailableModels());
                        unavailable.addAll(old.models());
                        unavailable.removeAll(listing.models());
                        unavailable.addAll(listing.unavailable());
                    }
                    return new Snapshot(listing.models(), unavailable, Instant.now(), true);
                });
                if (store != null) store.save(endpoint, updated);
            } catch (Exception failure) {
                snapshots.compute(endpoint.getId(), (id, old) -> old == null
                        ? new Snapshot(Set.of(), Set.of(), null, false) : new Snapshot(old.models(), old.unavailableModels(), old.updatedAt(), false));
                log.warn("AI model catalog refresh failed for {} ({})", endpoint.getId(), failure.getClass().getSimpleName());
            }
        }
    }

    public boolean isUnavailable(String endpoint, String model) {
        Snapshot snapshot = snapshots.get(endpoint);
        return snapshot != null && snapshot.unavailableModels().contains(model);
    }

    private static boolean retired(JSONObject model) {
        for (String field : List.of("status", "lifecycle")) {
            if (model.getString(field) != null && Set.of("deprecated", "retired", "disabled", "unavailable").contains(model.getString(field))) return true;
        }
        if (Boolean.TRUE.equals(model.get("deprecated"))) return true;
        String expires = model.getString("expiration_date");
        if (expires != null && !expires.isBlank()) {
            try {
                Instant expiry = expires.length() == 10 ? java.time.LocalDate.parse(expires).atStartOfDay(java.time.ZoneOffset.UTC).toInstant()
                        : Instant.parse(expires);
                return !expiry.isAfter(Instant.now());
            } catch (java.time.format.DateTimeParseException ignored) {
                // An unknown vendor date format is not evidence of retirement.
            }
        }
        return false;
    }

    private Listing fetch(AiProperties.Endpoint endpoint) {
        Set<String> ids = new LinkedHashSet<>();
        Set<String> unavailable = new HashSet<>();
        Set<String> cursors = new HashSet<>();
        URI uri = endpoint.catalogUri();
        for (int page = 0; page < 100; page++) {
            ResponseEntity<String> response = http.exchange(uri, HttpMethod.GET,
                    new HttpEntity<>(endpoint.getProtocol().headers(endpoint)), String.class);
            if (!response.getStatusCode().is2xxSuccessful()) throw new IllegalStateException("Catalog HTTP status");
            JSONObject body = JSONObject.parseObject(response.getBody());
            if (body == null) throw new IllegalStateException("Missing catalog response");
            boolean googleFormat = body.containsKey("models");
            JSONArray models = body.getJSONArray(googleFormat ? "models" : "data");
            if (models == null) throw new IllegalStateException("Missing catalog models");
            for (int i = 0; i < models.size(); i++) {
                JSONObject model = models.getJSONObject(i);
                String id = model.getString(googleFormat ? "name" : "id");
                if (id == null || id.isBlank() || id.length() > 256) throw new IllegalStateException("Invalid catalog model");
                id = id.startsWith("models/") ? id.substring(7) : id;
                ids.add(id);
                if (retired(model)) unavailable.add(id);
            }
            String cursor = body.getString("nextPageToken");
            String parameter = "pageToken";
            if (Boolean.TRUE.equals(body.getBoolean("has_more"))) {
                cursor = body.getString("last_id");
                parameter = "after_id";
                if (cursor == null || cursor.isBlank()) throw new IllegalStateException("Missing catalog cursor");
            }
            if (cursor == null || cursor.isBlank()) return new Listing(ids, unavailable);
            if (!cursors.add(cursor)) throw new IllegalStateException("Repeated catalog cursor");
            // Only accept cursor data, never a remote next-page URL.
            uri = UriComponentsBuilder.fromUri(endpoint.catalogUri()).queryParam(parameter, "{cursor}")
                    .encode().buildAndExpand(cursor).toUri();
        }
        throw new IllegalStateException("Catalog pagination limit exceeded");
    }
}
