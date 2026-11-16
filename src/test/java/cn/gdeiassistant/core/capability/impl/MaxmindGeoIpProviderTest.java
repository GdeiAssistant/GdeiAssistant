package cn.gdeiassistant.core.capability.impl;

import cn.gdeiassistant.common.config.application.OutboundIntegrationProperties;
import cn.gdeiassistant.common.exception.ProviderException;
import cn.gdeiassistant.core.capability.ai.AiProperties;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MaxmindGeoIpProviderTest {

    @TempDir
    Path tempDir;

    @Test
    void resolvesIpv4AndIpv6LocallyUnderMainlandRestriction() throws Exception {
        var database = new ClassPathResource("geoip/GeoLite2-City-Test.mmdb").getFile();
        try (var maxmind = new MaxmindGeoIpProvider(database.getPath())) {
            assertTrue(maxmind.isConfigured());
            var transport = new RestTemplate();
            var server = MockRestServiceServer.bindTo(transport).build();
            var settings = new AiProperties();
            var service = new IpLocationService(maxmind, new IpApiProvider(transport, settings),
                    new IpWhoisProvider(transport, settings), new SimpleMeterRegistry(), CircuitBreakerRegistry.ofDefaults(),
                    Runnable::run, new OutboundIntegrationProperties());

            var ipv4 = service.resolve("175.16.199.1");
            assertEquals("中国", ipv4.getCountry());
            assertEquals("吉林", ipv4.getProvince());
            assertEquals("长春", ipv4.getCity());
            assertEquals("-", ipv4.getNetwork());
            assertEquals("Boxford", service.resolve("2.125.160.216").getCity());
            assertEquals("日本", service.resolve("2001:218::1").getCountry());
            assertEquals("-", service.resolve("192.0.2.1").getCountry());
            assertThrows(ProviderException.class, () -> maxmind.execute("example.invalid"));
            server.verify();
        }
    }

    @Test
    void missingOrUnreadableDatabaseDoesNotEnableProvider() throws Exception {
        var missing = tempDir.resolve("missing.mmdb");
        var corrupt = Files.writeString(tempDir.resolve("corrupt.mmdb"), "invalid test database");

        for (Path path : List.of(missing, corrupt)) {
            try (var provider = new MaxmindGeoIpProvider(path.toString())) {
                assertFalse(provider.isConfigured());
                assertThrows(ProviderException.class, () -> provider.execute("192.0.2.1"));
            }
        }
    }
}
