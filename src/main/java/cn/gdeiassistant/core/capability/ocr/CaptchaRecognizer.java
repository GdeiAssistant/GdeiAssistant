package cn.gdeiassistant.core.capability.ocr;

import cn.gdeiassistant.common.enums.Recognition.CheckCodeTypeEnum;
import cn.gdeiassistant.common.exception.RecognitionException.RecognitionException;

public interface CaptchaRecognizer {

    String recognize(String imageBase64, CheckCodeTypeEnum typeEnum, int length) throws RecognitionException;
}
