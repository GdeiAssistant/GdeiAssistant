package cn.gdeiassistant.core.capability.impl;

import cn.gdeiassistant.common.exception.ProviderChainExhaustedException;
import cn.gdeiassistant.common.exception.ProviderException;
import cn.gdeiassistant.core.capability.ai.AiRequest;
import cn.gdeiassistant.core.capability.ai.AiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Map;

/** OCR owns recognition constraints and the local fallback; AI transport is shared with other features. */
@Component
public class OcrService {
    private static final Logger log = LoggerFactory.getLogger(OcrService.class);
    private final AiService ai;
    private final LocalCaptchaOcrProvider local;
    public OcrService(AiService ai, LocalCaptchaOcrProvider local) { this.ai = ai; this.local = local; }
    public String recognizeCaptcha(String imageBase64, String typeHint, int length) {
        return recognize(OcrRequest.forCaptcha(imageBase64, typeHint, length));
    }
    public String recognizeDigits(String imageBase64) { return recognize(OcrRequest.forDigits(imageBase64)); }
    private boolean valid(OcrRequest request, String result) {
        return result != null && !result.isBlank() && (request.getLength() > 0
                ? result.trim().matches("[A-Za-z0-9]{" + request.getLength() + "}") : result.trim().matches("[0-9]+"));
    }
    private String recognize(OcrRequest request) {
        if (request.getImageBase64() == null || request.getImageBase64().isBlank()) return "";
        try {
            return ai.generate("ocr", AiRequest.image(request.getPrompt(), "image/png", request.getImageBase64()),
                    result -> valid(request, result));
        } catch (ProviderChainExhaustedException failure) {
            try {
                String result = local.execute(request);
                if (valid(request, result)) return result;
            } catch (ProviderException ignored) {
                // Optional recognition failure: never log images or upstream response bodies.
            }
            log.warn("OCR recognition unavailable from configured AI models and local engine");
            return "";
        }
    }
    public List<Map<String, Object>> getProviderStatus() { return ai.providerStatus("ocr"); }
}
