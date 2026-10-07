package cn.gdeiassistant.core.capability.impl;
import cn.gdeiassistant.common.exception.ProviderException;
import cn.gdeiassistant.common.pojo.entity.IPAddressRecord;
import cn.gdeiassistant.core.capability.ServiceProvider;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;
class IpProviderContractsTest {
    @Test void freeProvidersNormalizeOptionalAndUnexpectedFieldsWithoutCallingRemoteSites() throws Exception {
        for(boolean whois:new boolean[]{true,false})for(String fields:new String[]{"\"country\":\"中国\",\"city\":\"合成城市\",\"region\":\"合成地区\",\"regionName\":\"合成地区\",\"isp\":\"合成网络\"","\"country\":{},\"city\":\"\",\"isp\":{}","\"country\":null"}) {
            var http=new RestTemplate();var server=MockRestServiceServer.bindTo(http).build();ServiceProvider<String,IPAddressRecord> provider=whois?new IpWhoisProvider():new IpApiProvider();ReflectionTestUtils.setField(provider,"restTemplate",http);
            assertTrue(provider.isConfigured());assertEquals(whois?0:1,provider.priority());assertEquals(whois?"ip-whois":"ip-api",provider.providerName());
            server.expect(requestTo(whois?"https://ipwho.is/192.0.2.1":"http://ip-api.com/json/192.0.2.1?fields=status,country,regionName,city,isp")).andRespond(withSuccess("{"+(whois?"\"success\":true,":"\"status\":\"success\",")+fields+"}",MediaType.APPLICATION_JSON));
            var record=provider.execute("192.0.2.1");assertNotNull(record.getCountry());assertFalse(record.getCity().isBlank());assertFalse(record.getProvince().isBlank());assertFalse(record.getNetwork().isBlank());assertEquals("-",record.getArea());server.verify();
        }
    }
    @Test void providerFailuresDoNotReturnFabricatedLocation(){
        for(boolean whois:new boolean[]{true,false})for(String body:new String[]{"{}","{\"success\":false,\"status\":\"fail\"}","invalid"}) {
            var http=new RestTemplate();var server=MockRestServiceServer.bindTo(http).build();ServiceProvider<String,IPAddressRecord> provider=whois?new IpWhoisProvider():new IpApiProvider();ReflectionTestUtils.setField(provider,"restTemplate",http);
            server.expect(anything()).andRespond(withSuccess(body,MediaType.APPLICATION_JSON));assertThrows(ProviderException.class,()->provider.execute("192.0.2.1"));server.verify();
        }
    }
}
