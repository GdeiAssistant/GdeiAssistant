package cn.gdeiassistant.core.capability.impl;

import cn.gdeiassistant.common.enums.Recognition.CheckCodeTypeEnum;
import cn.gdeiassistant.common.exception.RecognitionException.RecognitionException;
import cn.gdeiassistant.core.capability.ocr.CaptchaRecognizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Primary
@Component
public class FallbackCaptchaRecognizer implements CaptchaRecognizer {

    private static final String FALLBACK_ERROR_MESSAGE = "图像识别服务异常，请稍后重试";

    private final Logger logger = LoggerFactory.getLogger(FallbackCaptchaRecognizer.class);

    private final CaptchaRecognizer aiCaptchaRecognizer;
    private final CaptchaRecognizer localCaptchaRecognizer;

    public FallbackCaptchaRecognizer(@Qualifier("aiCaptchaRecognizer") CaptchaRecognizer aiCaptchaRecognizer,
                                     @Qualifier("localCaptchaRecognizer") CaptchaRecognizer localCaptchaRecognizer) {
        this.aiCaptchaRecognizer = aiCaptchaRecognizer;
        this.localCaptchaRecognizer = localCaptchaRecognizer;
    }

    @Override
    public String recognize(String imageBase64, CheckCodeTypeEnum typeEnum, int length) throws RecognitionException {
        try {
            return aiCaptchaRecognizer.recognize(imageBase64, typeEnum, length);
        } catch (RecognitionException aiException) {
            logger.warn("AI验证码识别失败，回退本地OCR：{}", aiException.getMessage());
            try {
                return localCaptchaRecognizer.recognize(imageBase64, typeEnum, length);
            } catch (RecognitionException localException) {
                logger.warn("本地OCR验证码识别也失败：{}", localException.getMessage());
                RecognitionException recognitionException = new RecognitionException(FALLBACK_ERROR_MESSAGE);
                recognitionException.addSuppressed(aiException);
                recognitionException.addSuppressed(localException);
                throw recognitionException;
            }
        }
    }
}
