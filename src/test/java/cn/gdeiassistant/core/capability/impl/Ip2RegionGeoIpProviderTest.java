package cn.gdeiassistant.core.capability.impl;

import cn.gdeiassistant.common.config.application.OutboundIntegrationProperties;
import cn.gdeiassistant.common.exception.ProviderException;
import cn.gdeiassistant.core.capability.ai.AiProperties;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.lionsoul.ip2region.service.Ip2Region;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

class Ip2RegionGeoIpProviderTest {

    @TempDir
    Path tempDir;

    @Test
    void resolvesOfflineUnderMainlandRestrictionAndRejectsHostnames() throws Exception {
        var searcher = mock(Ip2Region.class);
        when(searcher.search("192.0.2.1")).thenReturn("中国|广东省|广州市|电信|CN");
        when(searcher.search("2001:db8::1")).thenReturn("Japan|Tokyo|0|0|JP");
        when(searcher.search("192.0.2.2")).thenReturn("0|0|0|0|0");
        var metrics = new SimpleMeterRegistry();
        try (var provider = new Ip2RegionGeoIpProvider(searcher)) {
            var transport = new RestTemplate();
            var server = MockRestServiceServer.bindTo(transport).build();
            var settings = new AiProperties();
            var service = new IpLocationService(provider, new IpApiProvider(transport, settings),
                    new IpWhoisProvider(transport, settings), metrics, CircuitBreakerRegistry.ofDefaults(),
                    Runnable::run, new OutboundIntegrationProperties());

            var ipv4 = service.resolve("192.0.2.1");
            assertEquals("中国", ipv4.getCountry());
            assertEquals("广东省", ipv4.getProvince());
            assertEquals("广州市", ipv4.getCity());
            assertEquals("电信", ipv4.getNetwork());

            var ipv6 = service.resolve("2001:db8::1");
            assertEquals("Japan", ipv6.getCountry());
            assertEquals("Tokyo", ipv6.getProvince());
            assertEquals("-", ipv6.getCity());
            assertEquals("-", service.resolve("192.0.2.2").getCountry());
            assertThrows(ProviderException.class, () -> provider.execute("example.invalid"));
            server.verify();
        } finally {
            metrics.close();
        }
        verify(searcher).search("192.0.2.1");
        verify(searcher).search("2001:db8::1");
        verify(searcher).search("192.0.2.2");
        verify(searcher).close();
        verifyNoMoreInteractions(searcher);
    }

    @Test
    void missingAndUnreadableFilesDoNotEnableProvider() throws Exception {
        var missing = tempDir.resolve("missing.xdb");
        var corrupt = Files.writeString(tempDir.resolve("corrupt.xdb"), "invalid test database");
        try (var provider = new Ip2RegionGeoIpProvider(missing.toString(), corrupt.toString())) {
            assertFalse(provider.isConfigured());
            assertThrows(ProviderException.class, () -> provider.execute("192.0.2.1"));
        }
    }
}
