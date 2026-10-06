package cn.gdeiassistant.core.imagerecognition.service;

import cn.gdeiassistant.common.enums.recognition.CheckCodeTypeEnum;
import cn.gdeiassistant.common.exception.recognitionexception.RecognitionException;
import cn.gdeiassistant.core.capability.ocr.CaptchaRecognizer;
import cn.gdeiassistant.core.capability.ocr.OcrNumberRecognizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ImageRecognitionService {

    @Autowired
    private CaptchaRecognizer captchaRecognizer;

    @Autowired
    private OcrNumberRecognizer ocrNumberRecognizer;

    /**
     * 神经网络识别验证码图片，返回验证码
     *
     * @param image
     * @param checkCodeTypeEnum
     * @param length
     * @return
     */
    public String checkCodeRecognize(String image, CheckCodeTypeEnum checkCodeTypeEnum, int length) throws RecognitionException {
        return captchaRecognizer.recognize(image, checkCodeTypeEnum, length);
    }

    /**
     * OCR识别图片中的数字，返回数字文本串
     *
     * @param image
     * @return
     */
    public String characterNumberRecognize(String image) throws RecognitionException {
        return ocrNumberRecognizer.recognizeNumbers(image);
    }
}