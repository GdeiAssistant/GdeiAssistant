package cn.gdeiassistant.common.tools.springutils;

import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/** Finite per-attempt budgets shared by optional outbound integrations. */
public final class OutboundHttpClients {
    private OutboundHttpClients() {}
    public static RestTemplate create(int connectMillis, int readMillis) {
        if (connectMillis <= 0 || readMillis <= 0) throw new IllegalArgumentException("HTTP timeouts must be positive");
        var factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(connectMillis);
        factory.setReadTimeout(readMillis);
        return new RestTemplate(factory);
    }
}
