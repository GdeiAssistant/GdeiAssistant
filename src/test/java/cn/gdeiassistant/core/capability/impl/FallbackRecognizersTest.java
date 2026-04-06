package cn.gdeiassistant.core.capability.impl;

import cn.gdeiassistant.common.enums.Recognition.CheckCodeTypeEnum;
import cn.gdeiassistant.common.exception.RecognitionException.RecognitionException;
import cn.gdeiassistant.core.capability.ocr.CaptchaRecognizer;
import cn.gdeiassistant.core.capability.ocr.OcrNumberRecognizer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class FallbackRecognizersTest {

    @Test
    void shouldUseLocalCaptchaRecognizerWhenAiFails() throws Exception {
        CaptchaRecognizer aiCaptchaRecognizer = mock(CaptchaRecognizer.class);
        CaptchaRecognizer localCaptchaRecognizer = mock(CaptchaRecognizer.class);
        when(aiCaptchaRecognizer.recognize("image", CheckCodeTypeEnum.ENGLISH_WITH_NUMBER, 4))
                .thenThrow(new RecognitionException("OpenAI 识别失败"));
        when(localCaptchaRecognizer.recognize("image", CheckCodeTypeEnum.ENGLISH_WITH_NUMBER, 4))
                .thenReturn("A1B2");

        FallbackCaptchaRecognizer recognizer = new FallbackCaptchaRecognizer(aiCaptchaRecognizer, localCaptchaRecognizer);

        assertEquals("A1B2", recognizer.recognize("image", CheckCodeTypeEnum.ENGLISH_WITH_NUMBER, 4));
        verify(localCaptchaRecognizer).recognize("image", CheckCodeTypeEnum.ENGLISH_WITH_NUMBER, 4);
    }

    @Test
    void shouldThrowFriendlyMessageWhenAllCaptchaRecognizersFail() throws Exception {
        CaptchaRecognizer aiCaptchaRecognizer = mock(CaptchaRecognizer.class);
        CaptchaRecognizer localCaptchaRecognizer = mock(CaptchaRecognizer.class);
        when(aiCaptchaRecognizer.recognize("image", CheckCodeTypeEnum.NUMBER, 4))
                .thenThrow(new RecognitionException("AI验证码识别失败"));
        when(localCaptchaRecognizer.recognize("image", CheckCodeTypeEnum.NUMBER, 4))
                .thenThrow(new RecognitionException("本地OCR识别验证码失败"));

        FallbackCaptchaRecognizer recognizer = new FallbackCaptchaRecognizer(aiCaptchaRecognizer, localCaptchaRecognizer);

        RecognitionException exception = assertThrows(RecognitionException.class,
                () -> recognizer.recognize("image", CheckCodeTypeEnum.NUMBER, 4));

        assertEquals("图像识别服务异常，请稍后重试", exception.getMessage());
        assertEquals(2, exception.getSuppressed().length);
    }

    @Test
    void shouldUseLocalNumberRecognizerWhenAiFails() throws Exception {
        OcrNumberRecognizer aiOcrNumberRecognizer = mock(OcrNumberRecognizer.class);
        OcrNumberRecognizer localOcrNumberRecognizer = mock(OcrNumberRecognizer.class);
        when(aiOcrNumberRecognizer.recognizeNumbers("image"))
                .thenThrow(new RecognitionException("AI数字识别失败"));
        when(localOcrNumberRecognizer.recognizeNumbers("image"))
                .thenReturn("0123456789");

        FallbackOcrNumberRecognizer recognizer = new FallbackOcrNumberRecognizer(aiOcrNumberRecognizer, localOcrNumberRecognizer);

        assertEquals("0123456789", recognizer.recognizeNumbers("image"));
        verify(localOcrNumberRecognizer).recognizeNumbers("image");
    }
}
