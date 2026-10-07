package cn.gdeiassistant.core.capability.impl;
import cn.gdeiassistant.common.config.application.OutboundIntegrationProperties;
import cn.gdeiassistant.common.exception.ProviderException;
import cn.gdeiassistant.common.exception.verificationexception.SendSMSException;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
class CapabilityFacadeFlowTest {
    private final SimpleMeterRegistry registry = new SimpleMeterRegistry();
    @AfterEach void closeRegistry() { registry.close(); }
    @Test void ocrRejectsInvalidOutputFallsBackAndReportsExhaustionWithoutFabrication() throws Exception {
        var deep=mock(DeepSeekOcrProvider.class);var doubao=mock(DoubaoOcrProvider.class);var gemini=mock(GeminiOcrProvider.class);var openai=mock(OpenAiOcrProvider.class);var claude=mock(ClaudeOcrProvider.class);var local=mock(LocalCaptchaOcrProvider.class);
        when(deep.providerName()).thenReturn("deepseek");when(deep.isConfigured()).thenReturn(true);when(deep.isHealthy()).thenReturn(true);when(deep.execute(any())).thenReturn("explanation instead of digits");
        when(gemini.providerName()).thenReturn("gemini");when(gemini.priority()).thenReturn(2);when(gemini.isConfigured()).thenReturn(true);when(gemini.isHealthy()).thenReturn(true);when(gemini.execute(any())).thenReturn("1234");
        {
            var service=new OcrService(deep,doubao,gemini,openai,claude,local,registry,CircuitBreakerRegistry.ofDefaults(),Runnable::run,new OutboundIntegrationProperties());
            assertEquals("1234",service.recognizeCaptcha("synthetic","digits",4));assertEquals("1234",service.recognizeDigits("synthetic"));assertNotNull(service.getChain());verify(gemini,times(2)).execute(any());
            when(deep.execute(any())).thenThrow(new ProviderException("synthetic outage"));when(gemini.execute(any())).thenThrow(new ProviderException("synthetic outage"));
            assertEquals("",service.recognizeDigits("synthetic"));assertEquals("",service.recognizeCaptcha("synthetic","digits",4));
        }
    }
    @Test void missingSmsServicesFailClearlyAndFallbackPreservesGlobalCountryContext() throws Exception {
        var tencent=mock(TencentSmsProvider.class);var aliyun=mock(AliyunSmsProvider.class);when(tencent.providerName()).thenReturn("tencent");when(aliyun.providerName()).thenReturn("aliyun");when(aliyun.priority()).thenReturn(1);
        {
            var service=new SmsService(tencent,aliyun,registry,CircuitBreakerRegistry.ofDefaults(),Runnable::run,new OutboundIntegrationProperties());
            assertThrows(SendSMSException.class,()->service.sendChina(123456,"00000000000"));assertThrows(SendSMSException.class,()->service.sendGlobal(123456,1,"0000000000"));assertNotNull(service.getChain());
            when(tencent.isConfigured()).thenReturn(true);when(aliyun.isConfigured()).thenReturn(true);when(tencent.isHealthy()).thenReturn(true);when(aliyun.isHealthy()).thenReturn(true);doThrow(new ProviderException("synthetic outage")).when(tencent).execute(any());
            var enabled=new SmsService(tencent,aliyun,registry,CircuitBreakerRegistry.ofDefaults(),Runnable::run,new OutboundIntegrationProperties());
            enabled.sendChina(123456,"00000000000");enabled.sendGlobal(123456,1,"0000000000");
            verify(aliyun).execute(argThat(r->r.isGlobal()&&r.getAreaCode()==1&&r.getPhone().equals("0000000000")));verify(aliyun).execute(argThat(r->!r.isGlobal()&&r.getCode()==123456));
        }
    }
    @Test void missingIpServicesProduceExplicitUnknownLocation(){
        var maxmind=mock(MaxmindGeoIpProvider.class);var api=mock(IpApiProvider.class);var whois=mock(IpWhoisProvider.class);
        {
            var service=new IpLocationService(maxmind,api,whois,registry,CircuitBreakerRegistry.ofDefaults(),Runnable::run,new OutboundIntegrationProperties());
            var record=service.resolve("192.0.2.1");assertEquals("-",record.getCountry());assertEquals("-",record.getArea());assertEquals("-",record.getNetwork());assertNotNull(service.getChain());
        }
    }
}
