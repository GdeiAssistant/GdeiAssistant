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

    private Region region = Region.MAINLAND;
    private List<Endpoint> endpoints = new ArrayList<>();

    public Region getRegion() { return region; }
    public void setRegion(Region region) { this.region = Objects.requireNonNull(region); }
    public List<Endpoint> getEndpoints() { return endpoints; }
    public void setEndpoints(List<Endpoint> value) { endpoints = List.copyOf(value); }
    public boolean permits(Endpoint endpoint) { return endpoint.region == region; }

    @PostConstruct
    public void validate() {
        Set<String> ids = new HashSet<>();
        for (Endpoint endpoint : endpoints) {
            if (endpoint.id == null || !endpoint.id.matches("[a-z0-9-]{1,64}") || !ids.add(endpoint.id)) {
                throw new IllegalArgumentException("AI endpoint IDs must be unique lowercase identifiers");
            }
            if (endpoint.region == null || endpoint.accessMode == null || endpoint.protocol == null) {
                throw new IllegalArgumentException("AI endpoint requires region, access-mode and protocol: " + endpoint.id);
            }
            secureBase(endpoint.baseUrl);
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
        public boolean isConfigured() { return apiKey != null && !apiKey.isBlank(); }
    }
}
