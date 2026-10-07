package cn.gdeiassistant.common.config.application;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/** Millisecond budgets for optional outbound APIs. */
@Component
@ConfigurationProperties(prefix="outbound.http")
public class OutboundIntegrationProperties {
    private int connectTimeoutMs = 3000;
    private int readTimeoutMs = 6000;
    private int providerBudgetMs = 15000;
    public int getConnectTimeoutMs() { return connectTimeoutMs; }
    public void setConnectTimeoutMs(int value) { connectTimeoutMs = positive(value); }
    public int getReadTimeoutMs() { return readTimeoutMs; }
    public void setReadTimeoutMs(int value) { readTimeoutMs = positive(value); }
    public int getProviderBudgetMs() { return providerBudgetMs; }
    public void setProviderBudgetMs(int value) { providerBudgetMs = positive(value); }
    private static int positive(int value) { if(value <= 0) throw new IllegalArgumentException("Timeout must be positive"); return value; }
}
