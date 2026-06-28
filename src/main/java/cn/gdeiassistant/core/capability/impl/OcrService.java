package cn.gdeiassistant.core.capability.impl;

import cn.gdeiassistant.common.exception.ProviderChainExhaustedException;
import cn.gdeiassistant.core.capability.ProviderChain;
import cn.gdeiassistant.core.capability.ServiceProvider;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class OcrService {

    private static final Logger log = LoggerFactory.getLogger(OcrService.class);

    private final ProviderChain<OcrRequest, String> chain;

    @Autowired
    public OcrService(
            DeepSeekOcrProvider deepSeekOcrProvider,
            DoubaoOcrProvider doubaoOcrProvider,
            GeminiOcrProvider geminiOcrProvider,
            OpenAiOcrProvider openAiOcrProvider,
            ClaudeOcrProvider claudeOcrProvider,
            LocalCaptchaOcrProvider localCaptchaOcrProvider,
            MeterRegistry meterRegistry,
            CircuitBreakerRegistry circuitBreakerRegistry) {
        List<ServiceProvider<OcrRequest, String>> providers = Arrays.asList(
                deepSeekOcrProvider,
                doubaoOcrProvider,
                geminiOcrProvider,
                openAiOcrProvider,
                claudeOcrProvider,
                localCaptchaOcrProvider
        );
        this.chain = new ProviderChain<>("ocr", providers, meterRegistry, circuitBreakerRegistry);
    }

    public String recognizeCaptcha(String imageBase64, String typeHint, int length) {
        try {
            OcrRequest request = OcrRequest.forCaptcha(imageBase64, typeHint, length);
            return chain.execute(request);
        } catch (ProviderChainExhaustedException e) {
            log.error("AI验证码识别 — 所有 provider 均不可用", e);
            return "";
        }
    }

    public String recognizeDigits(String imageBase64) {
        try {
            OcrRequest request = OcrRequest.forDigits(imageBase64);
            return chain.execute(request);
        } catch (ProviderChainExhaustedException e) {
            log.error("AI数字识别 — 所有 provider 均不可用", e);
            return "";
        }
    }

    public ProviderChain<OcrRequest, String> getChain() {
        return chain;
    }
}
