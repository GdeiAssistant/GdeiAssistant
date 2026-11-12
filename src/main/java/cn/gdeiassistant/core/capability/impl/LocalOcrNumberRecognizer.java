package cn.gdeiassistant.core.capability.impl;

import cn.gdeiassistant.common.exception.recognitionexception.RecognitionException;
import cn.gdeiassistant.core.capability.ocr.OcrNumberRecognizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import cn.gdeiassistant.common.i18n.Messages;

@Component("localOcrNumberRecognizer")
public class LocalOcrNumberRecognizer implements OcrNumberRecognizer {

    @Autowired
    private LocalTemplateOcrEngine localTemplateOcrEngine;

    @Override
    public String recognizeNumbers(String imageBase64) throws RecognitionException {
        String text = localTemplateOcrEngine.recognizeNumbers(imageBase64);
        text = text == null ? "" : text.replaceAll("[^0-9]", "");
        if (text.isEmpty()) {
            throw new RecognitionException(Messages.get("capability.localOcrDigitFailed"));
        }
        return text;
    }
}
