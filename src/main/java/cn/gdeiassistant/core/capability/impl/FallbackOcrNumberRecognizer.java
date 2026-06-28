package cn.gdeiassistant.core.capability.impl;

import cn.gdeiassistant.common.exception.RecognitionException.RecognitionException;
import cn.gdeiassistant.core.capability.ocr.OcrNumberRecognizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

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
            throw new RecognitionException("图像识别服务异常，请稍后重试");
        }
        return text;
    }
}
