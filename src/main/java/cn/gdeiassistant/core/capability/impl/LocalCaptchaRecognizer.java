package cn.gdeiassistant.core.capability.impl;

import cn.gdeiassistant.common.enums.Recognition.CheckCodeTypeEnum;
import cn.gdeiassistant.common.exception.RecognitionException.RecognitionException;
import cn.gdeiassistant.common.tools.Utils.StringUtils;
import cn.gdeiassistant.core.capability.ocr.CaptchaRecognizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component("localCaptchaRecognizer")
public class LocalCaptchaRecognizer implements CaptchaRecognizer {

    @Autowired
    private LocalTemplateOcrEngine localTemplateOcrEngine;

    @Override
    public String recognize(String imageBase64, CheckCodeTypeEnum typeEnum, int length) throws RecognitionException {
        String text = localTemplateOcrEngine.recognizeCaptcha(imageBase64, typeEnum, length);
        text = sanitize(text, typeEnum, length);
        if (StringUtils.isBlank(text)) {
            throw new RecognitionException("本地OCR识别验证码失败");
        }
        return text;
    }

    private String sanitize(String raw, CheckCodeTypeEnum typeEnum, int length) {
        if (raw == null) {
            return "";
        }
        String text = raw.replaceAll("[^A-Za-z0-9\\u4e00-\\u9fa5]", "").trim();
        if (typeEnum == CheckCodeTypeEnum.NUMBER) {
            text = text.replaceAll("[^0-9]", "");
        } else if (typeEnum == CheckCodeTypeEnum.ENGLISH) {
            text = text.replaceAll("[^A-Za-z]", "");
        } else if (typeEnum == CheckCodeTypeEnum.ENGLISH_WITH_NUMBER) {
            text = text.replaceAll("[^A-Za-z0-9]", "");
        } else if (typeEnum == CheckCodeTypeEnum.CHINESE) {
            text = text.replaceAll("[^\\u4e00-\\u9fa5]", "");
        }
        if (length > 0 && text.length() > length) {
            text = text.substring(0, length);
        }
        if (length > 0 && text.length() < length) {
            return "";
        }
        return text.toUpperCase(Locale.ROOT);
    }
}
