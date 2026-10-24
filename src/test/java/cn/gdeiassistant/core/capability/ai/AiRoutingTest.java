package cn.gdeiassistant.core.capability.ai;

import cn.gdeiassistant.common.config.application.OutboundIntegrationProperties;
import cn.gdeiassistant.common.exception.ProviderException;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class AiRoutingTest {
    static AiProperties.Endpoint endpoint(String id, AiProperties.Region region, AiProtocol protocol) {
        var endpoint=new AiProperties.Endpoint();endpoint.setId(id);endpoint.setRegion(region);
        endpoint.setAccessMode(AiProperties.AccessMode.DIRECT);endpoint.setProtocol(protocol);
        endpoint.setBaseUrl("https://synthetic.invalid/v1");endpoint.setApiKey("synthetic-only");
        endpoint.setRoutes(Map.of("ocr",List.of("vision-one","vision-two")));return endpoint;
    }
    @Test void deployedConfigurationBindsAndDefaultsToMainlandWithoutCredentials() throws Exception {
        var environment=new StandardEnvironment();
        new YamlPropertySourceLoader().load("application",new ClassPathResource("application.yml"))
                .forEach(source->environment.getPropertySources().addLast(source));
        var settings=Binder.get(environment).bind("api.ai",AiProperties.class).get();
        settings.validate();assertEquals(AiProperties.Region.MAINLAND,settings.getRegion());
        assertTrue(settings.isCatalogEnabled());assertEquals(19,settings.getEndpoints().size());
        var code=settings.getEndpoints().stream().filter(e->e.getId().equals("opencode-claude")).findFirst().orElseThrow();
        assertEquals(AiProperties.AccessMode.AGGREGATOR,code.getAccessMode());
        assertEquals(AiProperties.Authentication.BEARER,code.getAuthentication());
        assertEquals("https://opencode.ai/inference/v1/models",code.catalogUri().toString());
        assertTrue(code.getRoutes().get("ocr").isEmpty());
    }
    @Test void regionPolicyFiltersBeforeNetworkAndDirectCallsAlsoFailClosed() {
        var settings=new AiProperties();
        var mainland=endpoint("china",AiProperties.Region.MAINLAND,AiProtocol.OPENAI_CHAT);
        var overseas=endpoint("global",AiProperties.Region.OVERSEAS,AiProtocol.ANTHROPIC);
        overseas.setAccessMode(AiProperties.AccessMode.AGGREGATOR);
        settings.setEndpoints(List.of(mainland,overseas));settings.validate();
        var registry=new AiProviderRegistry(settings,new OutboundIntegrationProperties());
        assertEquals(List.of("china/vision-one","china/vision-two"),registry.providers("ocr",new AiModelCatalog(settings,registry)).stream().map(p->p.providerName()).toList());
        var http=new RestTemplate();var server=MockRestServiceServer.bindTo(http).build();
        var blocked=new AiModelProvider(settings,overseas,"vision-one",http);
        assertFalse(blocked.isConfigured());
        assertThrows(ProviderException.class,()->blocked.execute(AiRequest.image("digits", "image/png", "private-image")));
        server.verify();
        settings.setRegion(AiProperties.Region.OVERSEAS);
        assertEquals(List.of("global/vision-one","global/vision-two"),registry.providers("ocr",new AiModelCatalog(settings,registry)).stream().map(p->p.providerName()).toList());
        mainland.setApiKey("");settings.setRegion(AiProperties.Region.MAINLAND);assertTrue(registry.providers("ocr",new AiModelCatalog(settings,registry)).isEmpty());
    }
    @Test void invalidOrAmbiguousConfigCannotStart() {
        for(String url:List.of("http://synthetic.invalid","https://user:secret@synthetic.invalid","https://synthetic.invalid?key=secret","https://synthetic.invalid#fragment","https://synthetic.invalid/../v1")) {
            var settings=new AiProperties();var endpoint=endpoint("a",AiProperties.Region.MAINLAND,AiProtocol.GEMINI);
            endpoint.setBaseUrl(url);settings.setEndpoints(List.of(endpoint));assertThrows(IllegalArgumentException.class,settings::validate);
        }
        for(String path:List.of("https://other.invalid/models","//other.invalid/models","/../models","/models?key=secret","/models#bad","/models\\bad")) {
            var settings=new AiProperties();var endpoint=endpoint("a",AiProperties.Region.MAINLAND,AiProtocol.GEMINI);
            endpoint.setCatalogPath(path);settings.setEndpoints(List.of(endpoint));assertThrows(IllegalArgumentException.class,settings::validate);
        }
        for(String model:List.of("../vision","vision?key=value","vision#fragment","vision/")) {
            var settings=new AiProperties();var endpoint=endpoint("a",AiProperties.Region.MAINLAND,AiProtocol.GEMINI);
            endpoint.setRoutes(Map.of("ocr",List.of(model)));settings.setEndpoints(List.of(endpoint));assertThrows(IllegalArgumentException.class,settings::validate);
        }
        var settings=new AiProperties();var endpoint=endpoint("a",AiProperties.Region.MAINLAND,AiProtocol.GEMINI);
        settings.setEndpoints(List.of(endpoint,endpoint));assertThrows(IllegalArgumentException.class,settings::validate);
        settings.setEndpoints(List.of(endpoint));endpoint.setRegion(null);assertThrows(IllegalArgumentException.class,settings::validate);
        endpoint.setRegion(AiProperties.Region.MAINLAND);endpoint.setPriority(-1);assertThrows(IllegalArgumentException.class,settings::validate);
        endpoint.setPriority(0);endpoint.setRoutes(Map.of("ocr",List.of("vision","vision")));assertThrows(IllegalArgumentException.class,settings::validate);
        endpoint.setId("Invalid ID");assertThrows(IllegalArgumentException.class,settings::validate);
    }
    @Test void productionHttpTransportDisablesRedirectsBeforeAnyNetworkCall() throws Exception {
        var registry=new AiProviderRegistry(new AiProperties(),new OutboundIntegrationProperties());
        var request=registry.http().getRequestFactory().createRequest(java.net.URI.create("https://synthetic.invalid/"),org.springframework.http.HttpMethod.GET);
        var connection=(java.net.HttpURLConnection)org.springframework.test.util.ReflectionTestUtils.getField(request,"connection");
        assertNotNull(connection);assertFalse(connection.getInstanceFollowRedirects());connection.disconnect();
    }

}
