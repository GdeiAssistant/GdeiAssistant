package cn.gdeiassistant.core.capability.impl;

import cn.gdeiassistant.common.exception.RecognitionException.RecognitionException;
import cn.gdeiassistant.core.capability.ocr.OcrNumberRecognizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("localOcrNumberRecognizer")
public class LocalOcrNumberRecognizer implements OcrNumberRecognizer {

    @Autowired
    private LocalTemplateOcrEngine localTemplateOcrEngine;

    @Override
    public String recognizeNumbers(String imageBase64) throws RecognitionException {
        String text = localTemplateOcrEngine.recognizeNumbers(imageBase64);
        text = text == null ? "" : text.replaceAll("[^0-9]", "");
        if (text.isEmpty()) {
            throw new RecognitionException("本地OCR数字识别失败");
        }
        return text;
    }
}
