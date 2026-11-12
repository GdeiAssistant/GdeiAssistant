package cn.gdeiassistant.core.capability.impl;

import cn.gdeiassistant.common.exception.recognitionexception.RecognitionException;
import cn.gdeiassistant.core.capability.ocr.OcrNumberRecognizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import cn.gdeiassistant.common.i18n.Messages;

@Primary
@Component
public class FallbackOcrNumberRecognizer implements OcrNumberRecognizer {

    @Autowired
    private OcrService ocrService;

    @Override
    public String recognizeNumbers(String imageBase64) throws RecognitionException {
        String text = ocrService.recognizeDigits(imageBase64);
        text = text == null ? "" : text.replaceAll("[^0-9]", "");
        if (text.isEmpty()) {
            throw new RecognitionException(Messages.get("graduateexam.recognitionUnavailable"));
        }
        return text;
    }
}
