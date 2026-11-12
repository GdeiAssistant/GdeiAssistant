package cn.gdeiassistant.core.capability.impl;

import cn.gdeiassistant.common.exception.ProviderException;
import cn.gdeiassistant.common.exception.recognitionexception.RecognitionException;
import cn.gdeiassistant.core.capability.ServiceProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import cn.gdeiassistant.common.i18n.Messages;

@Component
@Order(5)
public class LocalCaptchaOcrProvider implements ServiceProvider<OcrRequest, String> {

    @Autowired
    private LocalTemplateOcrEngine localTemplateOcrEngine;

    @Override
    public String providerName() {
        return "local-ocr";
    }

    @Override
    public int priority() {
        return 5;
    }

    @Override
    public String execute(OcrRequest request) throws ProviderException {
        try {
            if (request.getTypeEnum() != null) {
                return localTemplateOcrEngine.recognizeCaptcha(
                        request.getImageBase64(), request.getTypeEnum(), request.getLength());
            } else {
                return localTemplateOcrEngine.recognizeNumbers(request.getImageBase64());
            }
        } catch (RecognitionException e) {
            throw new ProviderException(Messages.get("capability.localOcrFailed"), e);
        }
    }

    @Override
    public boolean isConfigured() {
        return true;
    }
}
