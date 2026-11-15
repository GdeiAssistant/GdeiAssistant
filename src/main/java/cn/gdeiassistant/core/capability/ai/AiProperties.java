package cn.gdeiassistant.core.capability.ai;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.*;

/** Server-owned routing configuration. Never bind these fields from a user request. */
@Component
@ConfigurationProperties(prefix = "api.ai")
public class AiProperties {
    public enum Region { MAINLAND, OVERSEAS }
    public enum AccessMode { DIRECT, AGGREGATOR }
    public enum Authentication { NATIVE, BEARER }
    public enum ThinkingMode { DEFAULT, ENABLED, DISABLED }

    /** Route-level execution policy (Layer 3): timeout budget and reasoning preference. */
    public static class RoutePolicy {
        private long timeoutMs;
        private ThinkingMode thinking = ThinkingMode.DEFAULT;

        public RoutePolicy() {}

        public RoutePolicy(long timeoutMs, ThinkingMode thinking) {
            this.timeoutMs = timeoutMs;
            this.thinking = Objects.requireNonNull(thinking);
        }

        public long getTimeoutMs() { return timeoutMs; }
        public void setTimeoutMs(long value) { timeoutMs = value; }
        public ThinkingMode getThinking() { return thinking; }
        public void setThinking(ThinkingMode value) { thinking = Objects.requireNonNull(value); }

        void validate() {
            if (timeoutMs < 0) throw new IllegalArgumentException("AI route timeoutMs cannot be negative");
            if (thinking == null) throw new IllegalArgumentException("AI route thinking mode is required");
        }
    }

    /** Resolved Layer-2 binding linking an approved Vendor endpoint, selected model, and Layer-3 RoutePolicy. */
    public record ModelBinding(Endpoint vendor, String model, RoutePolicy policy) {}

    private boolean autoUpdate = true;
    private boolean regionRestrictionEnabled = true;
    private Region region = Region.MAINLAND;
    private Map<String, RoutePolicy> routePolicies = defaultRoutePolicies();
    private List<Endpoint> endpoints = new ArrayList<>();

    private static Map<String, RoutePolicy> defaultRoutePolicies() {
        var defaults = new LinkedHashMap<String, RoutePolicy>();
        defaults.put("ocr", new RoutePolicy(6000L, ThinkingMode.DISABLED));
        defaults.put("chat", new RoutePolicy(25000L, ThinkingMode.DEFAULT));
        defaults.put("translate", new RoutePolicy(25000L, ThinkingMode.DISABLED));
        return Collections.unmodifiableMap(defaults);
    }

    public boolean isAutoUpdate() { return autoUpdate; }
    public void setAutoUpdate(boolean value) { autoUpdate = value; }
    public boolean isRegionRestrictionEnabled() { return regionRestrictionEnabled; }
    public void setRegionRestrictionEnabled(boolean value) { regionRestrictionEnabled = value; }
    public Region getRegion() { return region; }
    public void setRegion(Region region) { this.region = Objects.requireNonNull(region); }
    public Map<String, RoutePolicy> getRoutePolicies() { return routePolicies; }
    public void setRoutePolicies(Map<String, RoutePolicy> value) {
        var merged = new LinkedHashMap<>(defaultRoutePolicies());
        if (value != null) {
            value.forEach((route, policy) -> {
                if (!route.matches("[a-z][a-z0-9-]{0,63}")) throw new IllegalArgumentException("Invalid AI route policy key");
                if (policy != null) {
                    policy.validate();
                    merged.put(route, new RoutePolicy(policy.getTimeoutMs(), policy.getThinking()));
                }
            });
        }
        routePolicies = Collections.unmodifiableMap(merged);
    }
    public RoutePolicy routePolicy(String route) {
        RoutePolicy policy = routePolicies.get(route);
        return policy != null ? policy : new RoutePolicy(0L, ThinkingMode.DEFAULT);
    }
    public List<Endpoint> getEndpoints() { return endpoints; }
    public void setEndpoints(List<Endpoint> value) { endpoints = List.copyOf(value); }
    public boolean permits(Endpoint endpoint) { return !regionRestrictionEnabled || endpoint.region == region; }

    @PostConstruct
    public void validate() {
        routePolicies.values().forEach(RoutePolicy::validate);
        Set<String> ids = new HashSet<>();
        for (Endpoint endpoint : endpoints) {
            if (endpoint.id == null || !endpoint.id.matches("[a-z0-9-]{1,64}") || !ids.add(endpoint.id)) {
                throw new IllegalArgumentException("AI endpoint IDs must be unique lowercase identifiers");
            }
            if (endpoint.region == null || endpoint.accessMode == null || endpoint.protocol == null) {
                throw new IllegalArgumentException("AI endpoint requires region, access-mode and protocol: " + endpoint.id);
            }
            secureBase(endpoint.baseUrl);
            if (!endpoint.catalogPath.matches("/[A-Za-z0-9/_-]+") || endpoint.catalogPath.contains("//")) {
                throw new IllegalArgumentException("Invalid AI catalog path");
            }
            endpoint.automaticRoutes.forEach((route, rules) -> rules.forEach(Selection::validate));
            if (endpoint.priority < 0 || endpoint.priority >= 1000) throw new IllegalArgumentException("Invalid AI priority");
            if (endpoint.routes.values().stream().anyMatch(models -> new HashSet<>(models).size() != models.size())) {
                throw new IllegalArgumentException("Duplicate AI models: " + endpoint.id);
            }
            for (String model : endpoint.routes.values().stream().flatMap(List::stream).toList()) {
                if (model == null || !model.matches("[A-Za-z0-9][A-Za-z0-9._:/-]{0,199}")
                        || model.contains("..") || model.endsWith("/")) {
                    throw new IllegalArgumentException("Invalid AI model ID: " + endpoint.id);
                }
            }
        }
    }

    private static void secureBase(String value) {
        URI uri = URI.create(value);
        if (!"https".equals(uri.getScheme()) || uri.getHost() == null || uri.getUserInfo() != null
                || uri.getQuery() != null || uri.getFragment() != null || value.contains("..")) {
            throw new IllegalArgumentException("AI base URL must be HTTPS without credentials, query or fragment");
        }
    }

    public static class Endpoint {
        private String id;
        private Region region;
        private AccessMode accessMode;
        private AiProtocol protocol;
        private Authentication authentication = Authentication.NATIVE;
        private String baseUrl = "";
        private String apiKey = "";
        private int priority;
        private boolean enabled = true;
        private Set<String> disabledModels = Set.of();
        private String catalogPath = "/models";
        private Map<String, List<Selection>> automaticRoutes = Map.of();
        private Map<String, List<String>> routes = new LinkedHashMap<>();
        public String getId() { return id; }
        public void setId(String value) { id = value; }
        public Region getRegion() { return region; }
        public void setRegion(Region value) { region = value; }
        public AccessMode getAccessMode() { return accessMode; }
        public void setAccessMode(AccessMode value) { accessMode = value; }
        public AiProtocol getProtocol() { return protocol; }
        public void setProtocol(AiProtocol value) { protocol = value; }
        public Authentication getAuthentication() { return authentication; }
        public void setAuthentication(Authentication value) { authentication = Objects.requireNonNull(value); }
        public String getBaseUrl() { return baseUrl; }
        public void setBaseUrl(String value) { baseUrl = value.replaceAll("/+$", ""); }
        public String getApiKey() { return apiKey; }
        public void setApiKey(String value) { apiKey = value; }
        public int getPriority() { return priority; }
        public void setPriority(int value) { priority = value; }
        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean value) { enabled = value; }
        public Set<String> getDisabledModels() { return disabledModels; }
        public void setDisabledModels(Set<String> value) {
            if (value == null || value.isEmpty()) {
                disabledModels = Set.of();
                return;
            }
            var copy = new LinkedHashSet<String>();
            for (String item : value) {
                if (item != null && !item.isBlank()) {
                    copy.add(item.trim());
                }
            }
            disabledModels = Collections.unmodifiableSet(copy);
        }
        /** Approved model queues by business route, e.g. ocr, chat, summary. */
        public Map<String, List<String>> getRoutes() { return routes; }
        public void setRoutes(Map<String, List<String>> value) {
            var copy = new LinkedHashMap<String, List<String>>();
            value.forEach((route, models) -> {
                if (!route.matches("[a-z][a-z0-9-]{0,63}")) throw new IllegalArgumentException("Invalid AI route");
                copy.put(route, List.copyOf(models));
            });
            routes = Collections.unmodifiableMap(copy);
        }
        public String getCatalogPath() { return catalogPath; }
        public void setCatalogPath(String value) { catalogPath = value; }
        public URI catalogUri() {
            // A leading /inference/ path is absolute on this approved host, never a remote URL.
            return catalogPath.equals("/models") ? URI.create(baseUrl + catalogPath) : URI.create(baseUrl).resolve(catalogPath);
        }
        public Map<String, List<Selection>> getAutomaticRoutes() { return automaticRoutes; }
        public void setAutomaticRoutes(Map<String, List<Selection>> value) {
            var copy = new LinkedHashMap<String, List<Selection>>();
            value.forEach((route, rules) -> {
                if (!route.matches("[a-z][a-z0-9-]{0,63}")) throw new IllegalArgumentException("Invalid AI route");
                copy.put(route, List.copyOf(rules));
            });
            automaticRoutes = Collections.unmodifiableMap(copy);
        }
        public boolean isConfigured() { return enabled && apiKey != null && !apiKey.isBlank(); }
    }

    /** Each rule follows one server-approved model family, using its official alias or numeric version. */
    public static class Selection {
        private String alias = "";
        private String pattern = "";
        private boolean imageInput;
        public String getAlias() { return alias; }
        public void setAlias(String value) { alias = value; }
        public String getPattern() { return pattern; }
        public void setPattern(String value) { pattern = value; }
        public boolean isImageInput() { return imageInput; }
        public void setImageInput(boolean value) { imageInput = value; }
        void validate() {
            if (alias.isBlank() == pattern.isBlank()) throw new IllegalArgumentException("Choose AI alias or version pattern");
            if (!alias.isBlank() && (!alias.matches("[A-Za-z0-9][A-Za-z0-9._:/-]{0,199}") || alias.contains(".."))) {
                throw new IllegalArgumentException("Invalid AI alias");
            }
            if (!pattern.isBlank()) {
                java.util.regex.Pattern.compile(pattern);
                if (!pattern.contains("(?<version>")) throw new IllegalArgumentException("AI pattern needs version group");
            }
        }
    }
}
