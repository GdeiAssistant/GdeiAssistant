package cn.gdeiassistant.core.capability.ocr;

import cn.gdeiassistant.common.enums.recognition.CheckCodeTypeEnum;
import cn.gdeiassistant.common.exception.recognitionexception.RecognitionException;

public interface CaptchaRecognizer {

    String recognize(String imageBase64, CheckCodeTypeEnum typeEnum, int length) throws RecognitionException;
}
