package cn.gdeiassistant.core.capability.impl;

import cn.gdeiassistant.common.enums.recognition.CheckCodeTypeEnum;
import cn.gdeiassistant.common.exception.recognitionexception.RecognitionException;
import cn.gdeiassistant.core.capability.ocr.CaptchaRecognizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import cn.gdeiassistant.common.i18n.Messages;

@Primary
@Component
public class FallbackCaptchaRecognizer implements CaptchaRecognizer {

    @Autowired
    private OcrService ocrService;

    @Override
    public String recognize(String imageBase64, CheckCodeTypeEnum typeEnum, int length) throws RecognitionException {
        String typeHint = typeEnum != null ? typeEnum.name() : "UNKNOWN";
        String text = ocrService.recognizeCaptcha(imageBase64, typeHint, length);
        if (text == null || text.isBlank()) {
            throw new RecognitionException(Messages.get("graduateexam.recognitionUnavailable"));
        }
        return text;
    }
}
