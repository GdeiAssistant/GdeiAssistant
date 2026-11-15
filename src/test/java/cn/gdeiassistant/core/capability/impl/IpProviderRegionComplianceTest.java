package cn.gdeiassistant.core.capability.impl;

import cn.gdeiassistant.common.exception.ProviderException;
import cn.gdeiassistant.core.capability.ai.AiProperties;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

class IpProviderRegionComplianceTest {

    @Test
    void mainlandRestrictedDisablesOverseasIpProvidersWithoutNetworkCalls() {
        AiProperties properties = new AiProperties();
        properties.setRegion(AiProperties.Region.MAINLAND);
        properties.setRegionRestrictionEnabled(true);

        RestTemplate restTemplate = mock(RestTemplate.class);
        IpApiProvider ipApi = new IpApiProvider(restTemplate, properties);
        IpWhoisProvider ipWhois = new IpWhoisProvider(restTemplate, properties);

        assertFalse(ipApi.isConfigured());
        assertFalse(ipWhois.isConfigured());
        assertThrows(ProviderException.class, () -> ipApi.execute("8.8.8.8"));
        assertThrows(ProviderException.class, () -> ipWhois.execute("8.8.8.8"));
        verifyNoInteractions(restTemplate);
    }

    @Test
    void globalOrUnrestrictedEnablesOverseasIpProviders() {
        AiProperties global = new AiProperties();
        global.setRegion(AiProperties.Region.OVERSEAS);
        global.setRegionRestrictionEnabled(true);

        AiProperties unrestricted = new AiProperties();
        unrestricted.setRegion(AiProperties.Region.MAINLAND);
        unrestricted.setRegionRestrictionEnabled(false);

        RestTemplate restTemplate = mock(RestTemplate.class);
        assertTrue(new IpApiProvider(restTemplate, global).isConfigured());
        assertTrue(new IpWhoisProvider(restTemplate, unrestricted).isConfigured());
    }
}
