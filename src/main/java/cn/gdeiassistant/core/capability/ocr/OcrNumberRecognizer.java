package cn.gdeiassistant.core.capability.ocr;

import cn.gdeiassistant.common.exception.recognitionexception.RecognitionException;

public interface OcrNumberRecognizer {

    String recognizeNumbers(String imageBase64) throws RecognitionException;
}
