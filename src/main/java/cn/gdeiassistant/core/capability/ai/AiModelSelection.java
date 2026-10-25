package cn.gdeiassistant.core.capability.ai;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigInteger;
import java.net.URI;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

/** Metadata GETs only. Remote entries cannot change credentials, regions, protocols or approved families. */
public class AiModelSelection {
    private static final Logger log = LoggerFactory.getLogger(AiModelSelection.class);
    private record Version(String number, String revision) {}
    private record Model(String id, boolean imageInput) {}
    private record Snapshot(List<Model> models, Instant updatedAt, boolean succeeded) {}
    public record Status(boolean catalogRequired, Instant updatedAt, Boolean lastRefreshSucceeded, Map<String, List<String>> selected) {}
    private final AiProperties settings;
    private final RestTemplate http;
    private final Map<String, Snapshot> snapshots = new ConcurrentHashMap<>();

    AiModelSelection(AiProperties settings, RestTemplate http) { this.settings = settings; this.http = http; }

    public List<String> models(AiProperties.Endpoint endpoint, String route, boolean images) {
        List<String> explicit = endpoint.getRoutes().getOrDefault(route, List.of());
        if (!explicit.isEmpty() || !settings.isAutoUpdate()) return explicit;
        Snapshot snapshot = snapshots.get(endpoint.getId());
        var selected = new LinkedHashSet<String>();
        for (AiProperties.Selection rule : endpoint.getAutomaticRoutes().getOrDefault(route, List.of())) {
            if (images && !rule.isImageInput()) continue;
            if (!rule.getAlias().isBlank()) {
                selected.add(rule.getAlias());
            } else if (snapshot != null) {
                Pattern pattern = Pattern.compile(rule.getPattern());
                snapshot.models().stream().filter(model -> !images || model.imageInput())
                        .filter(model -> pattern.matcher(model.id()).matches())
                        .max((a, b) -> {
                            int compared = compareVersions(version(pattern, a.id()), version(pattern, b.id()));
                            return compared != 0 ? compared : a.id().compareTo(b.id());
                        })
                        .ifPresent(model -> selected.add(model.id()));
            }
        }
        return List.copyOf(selected);
    }

    private static Version version(Pattern pattern, String id) {
        var match = pattern.matcher(id);
        if (!match.matches()) throw new IllegalArgumentException("Model does not match family");
        String number = match.group("version");
        if (number == null || !number.matches("[0-9]+(?:[.-][0-9]+)*")) throw new IllegalArgumentException("Invalid model version");
        String revision = pattern.pattern().contains("(?<revision>") ? match.group("revision") : null;
        if (revision != null && !revision.matches("[0-9]+")) throw new IllegalArgumentException("Invalid model revision");
        return new Version(number, revision == null ? "0" : revision);
    }

    private static int compareVersions(Version a, Version b) {
        String[] left = a.number().split("[.-]"), right = b.number().split("[.-]");
        for (int i = 0; i < Math.max(left.length, right.length); i++) {
            int result = new BigInteger(i < left.length ? left[i] : "0").compareTo(new BigInteger(i < right.length ? right[i] : "0"));
            if (result != 0) return result;
        }
        // A snapshot date only breaks a tie within the same release, never outranks the next minor release.
        return new BigInteger(a.revision()).compareTo(new BigInteger(b.revision()));
    }

    public void refresh() {
        if (!settings.isAutoUpdate()) return;
        for (AiProperties.Endpoint endpoint : settings.getEndpoints()) {
            if (!settings.permits(endpoint) || !endpoint.isConfigured() || !needsCatalog(endpoint)) continue;
            try {
                List<Model> models = fetch(endpoint);
                // Validate every matching version before publishing a complete, atomic replacement.
                endpoint.getAutomaticRoutes().values().stream().flatMap(List::stream)
                        .filter(rule -> !rule.getPattern().isBlank()).forEach(rule -> {
                            Pattern pattern = Pattern.compile(rule.getPattern());
                            models.stream().filter(model -> pattern.matcher(model.id()).matches()).forEach(model -> version(pattern, model.id()));
                        });
                snapshots.put(endpoint.getId(), new Snapshot(models, Instant.now(), true));
            } catch (Exception failure) {
                snapshots.compute(endpoint.getId(), (id, old) -> old == null ? new Snapshot(List.of(), null, false)
                        : new Snapshot(old.models(), old.updatedAt(), false));
                // Do not log response bodies, URLs, credentials or input data.
                log.warn("AI model refresh failed for {} ({})", endpoint.getId(), failure.getClass().getSimpleName());
            }
        }
    }

    private static boolean needsCatalog(AiProperties.Endpoint endpoint) {
        return endpoint.getAutomaticRoutes().entrySet().stream()
                .filter(entry -> endpoint.getRoutes().getOrDefault(entry.getKey(), List.of()).isEmpty())
                .flatMap(entry -> entry.getValue().stream()).anyMatch(rule -> !rule.getPattern().isBlank());
    }

    public Map<String, Status> status() {
        var result = new LinkedHashMap<String, Status>();
        settings.getEndpoints().stream().filter(settings::permits).filter(AiProperties.Endpoint::isConfigured).forEach(endpoint -> {
            var selected = new LinkedHashMap<String, List<String>>();
            var routes = new TreeSet<>(endpoint.getAutomaticRoutes().keySet());
            routes.addAll(endpoint.getRoutes().keySet());
            routes.forEach(route -> selected.put(route, models(endpoint, route, false)));
            Snapshot snapshot = snapshots.get(endpoint.getId());
            result.put(endpoint.getId(), new Status(settings.isAutoUpdate() && needsCatalog(endpoint), snapshot == null ? null : snapshot.updatedAt(),
                    snapshot == null ? null : snapshot.succeeded(), Collections.unmodifiableMap(selected)));
        });
        return Collections.unmodifiableMap(result);
    }

    private List<Model> fetch(AiProperties.Endpoint endpoint) {
        var result = new LinkedHashMap<String, Model>();
        Set<String> cursors = new HashSet<>();
        URI uri = endpoint.catalogUri();
        for (int page = 0; page < 100; page++) {
            ResponseEntity<String> response = http.exchange(uri, HttpMethod.GET,
                    new HttpEntity<>(endpoint.getProtocol().headers(endpoint)), String.class);
            if (!response.getStatusCode().is2xxSuccessful()) throw new IllegalStateException("Catalog HTTP status");
            JSONObject body = JSONObject.parseObject(response.getBody());
            if (body == null) throw new IllegalStateException("Missing catalog response");
            boolean google = body.containsKey("models");
            JSONArray rows = body.getJSONArray(google ? "models" : "data");
            if (rows == null) throw new IllegalStateException("Missing catalog models");
            for (int i = 0; i < rows.size(); i++) {
                JSONObject row = rows.getJSONObject(i);
                String id = row.getString(google ? "name" : "id");
                if (id != null && id.startsWith("models/")) id = id.substring(7);
                if (id == null || !id.matches("[A-Za-z0-9][A-Za-z0-9._:/-]{0,199}") || id.contains("..")) {
                    throw new IllegalStateException("Invalid catalog model");
                }
                if (retired(row)) continue;
                JSONArray methods = row.getJSONArray("supportedGenerationMethods");
                if (google && methods != null && !methods.contains("generateContent")) continue;
                JSONObject architecture = row.getJSONObject("architecture");
                if (architecture != null && architecture.getJSONArray("output_modalities") != null
                        && !architecture.getJSONArray("output_modalities").contains("text")) continue;
                boolean image = true; // Family approval is authoritative when the vendor omits modality metadata.
                JSONObject capabilities = row.getJSONObject("capabilities");
                if (capabilities != null && capabilities.getJSONObject("image_input") != null) {
                    image = Boolean.TRUE.equals(capabilities.getJSONObject("image_input").getBoolean("supported"));
                }
                if (architecture != null && architecture.getJSONArray("input_modalities") != null) {
                    image = architecture.getJSONArray("input_modalities").contains("image");
                }
                result.put(id, new Model(id, image));
                if (result.size() > 20000) throw new IllegalStateException("Catalog size limit exceeded");
            }
            String cursor = body.getString("nextPageToken"), parameter = "pageToken";
            if (Boolean.TRUE.equals(body.getBoolean("has_more"))) {
                cursor = body.getString("last_id"); parameter = "after_id";
                if (cursor == null || cursor.isBlank()) throw new IllegalStateException("Missing catalog cursor");
            }
            if (cursor == null || cursor.isBlank()) return List.copyOf(result.values());
            if (!cursors.add(cursor)) throw new IllegalStateException("Repeated catalog cursor");
            uri = UriComponentsBuilder.fromUri(endpoint.catalogUri()).queryParam(parameter, "{cursor}")
                    .encode().buildAndExpand(cursor).toUri();
        }
        throw new IllegalStateException("Catalog pagination limit exceeded");
    }

    private static boolean retired(JSONObject model) {
        for (String field : List.of("status", "lifecycle")) {
            String state = model.getString(field);
            if (state != null && Set.of("deprecated", "retired", "disabled", "unavailable", "preview", "experimental").contains(state.toLowerCase(Locale.ROOT))) return true;
        }
        if (Boolean.TRUE.equals(model.get("deprecated"))) return true;
        for (String field : List.of("expiration_date", "retires_at")) {
            String date = model.getString(field);
            if (date == null || date.isBlank()) continue;
            try {
                Instant expiry = date.length() == 10 ? java.time.LocalDate.parse(date).atStartOfDay(java.time.ZoneOffset.UTC).toInstant() : Instant.parse(date);
                if (!expiry.isAfter(Instant.now())) return true;
            } catch (java.time.format.DateTimeParseException ignored) {
                // Unknown date formats are not evidence of retirement.
            }
        }
        return false;
    }
}
