package cn.gdeiassistant.common.constant;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Observability thresholds. Static constants provide compile-time defaults;
 * the Spring bean fields are injected from observability.* properties so
 * operators can tune thresholds without rebuilding.
 */
@Component
public class ObservabilityConstants {

    // Static defaults (used by any code that cannot inject this bean)
    public static final long REST_SLOW_THRESHOLD_MS = 800;
    public static final long QUERY_SLOW_THRESHOLD_MS = 1500;

    private final long slowRequestThresholdMs;

    private final long slowQueryThresholdMs;

    public ObservabilityConstants(@Value("${observability.slow-request-threshold-ms:800}") long slowRequestThresholdMs, @Value("${observability.slow-query-threshold-ms:1500}") long slowQueryThresholdMs) {
        this.slowRequestThresholdMs = slowRequestThresholdMs;
        this.slowQueryThresholdMs = slowQueryThresholdMs;
    }

    public long getSlowRequestThresholdMs() {
        return slowRequestThresholdMs;
    }

    public long getSlowQueryThresholdMs() {
        return slowQueryThresholdMs;
    }
}
