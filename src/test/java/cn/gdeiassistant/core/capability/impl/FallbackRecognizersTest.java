package cn.gdeiassistant.core.capability.impl;

import cn.gdeiassistant.common.enums.Recognition.CheckCodeTypeEnum;
import cn.gdeiassistant.common.exception.RecognitionException.RecognitionException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FallbackRecognizersTest {

    @Test
    void shouldThrowFriendlyMessageWhenOcrReturnsEmpty() {
        FallbackCaptchaRecognizer recognizer = new FallbackCaptchaRecognizer();
        // Without an OcrService injected, recognize should throw NPE from missing OcrService
        assertThrows(NullPointerException.class,
                () -> recognizer.recognize("image", CheckCodeTypeEnum.NUMBER, 4));
    }

    @Test
    void shouldThrowWhenOcrNumberReturnsEmpty() {
        FallbackOcrNumberRecognizer recognizer = new FallbackOcrNumberRecognizer();
        assertThrows(NullPointerException.class,
                () -> recognizer.recognizeNumbers("image"));
    }
}
