package cn.gdeiassistant.core.capability.impl;

import cn.gdeiassistant.common.exception.RecognitionException.RecognitionException;
import cn.gdeiassistant.core.capability.ocr.OcrNumberRecognizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Primary
@Component
public class FallbackOcrNumberRecognizer implements OcrNumberRecognizer {

    private static final String FALLBACK_ERROR_MESSAGE = "图像识别服务异常，请稍后重试";

    private final Logger logger = LoggerFactory.getLogger(FallbackOcrNumberRecognizer.class);

    private final OcrNumberRecognizer aiOcrNumberRecognizer;
    private final OcrNumberRecognizer localOcrNumberRecognizer;

    public FallbackOcrNumberRecognizer(@Qualifier("aiOcrNumberRecognizer") OcrNumberRecognizer aiOcrNumberRecognizer,
                                       @Qualifier("localOcrNumberRecognizer") OcrNumberRecognizer localOcrNumberRecognizer) {
        this.aiOcrNumberRecognizer = aiOcrNumberRecognizer;
        this.localOcrNumberRecognizer = localOcrNumberRecognizer;
    }

    @Override
    public String recognizeNumbers(String imageBase64) throws RecognitionException {
        try {
            return aiOcrNumberRecognizer.recognizeNumbers(imageBase64);
        } catch (RecognitionException aiException) {
            logger.warn("AI数字识别失败，回退本地OCR：{}", aiException.getMessage());
            try {
                return localOcrNumberRecognizer.recognizeNumbers(imageBase64);
            } catch (RecognitionException localException) {
                logger.warn("本地OCR数字识别也失败：{}", localException.getMessage());
                RecognitionException recognitionException = new RecognitionException(FALLBACK_ERROR_MESSAGE);
                recognitionException.addSuppressed(aiException);
                recognitionException.addSuppressed(localException);
                throw recognitionException;
            }
        }
    }
}
