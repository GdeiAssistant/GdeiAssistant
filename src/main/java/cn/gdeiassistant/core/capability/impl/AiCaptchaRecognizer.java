package cn.gdeiassistant.core.capability.impl;

import cn.gdeiassistant.common.enums.Recognition.CheckCodeTypeEnum;
import cn.gdeiassistant.common.exception.RecognitionException.RecognitionException;
import cn.gdeiassistant.common.tools.Utils.StringUtils;
import cn.gdeiassistant.core.capability.ocr.CaptchaRecognizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AiCaptchaRecognizer implements CaptchaRecognizer {

    @Autowired
    private AiLlmVisionClient aiLlmVisionClient;

    @Override
    public String recognize(String imageBase64, CheckCodeTypeEnum typeEnum, int length) throws RecognitionException {
        String typeHint = typeEnum != null ? typeEnum.name() : "UNKNOWN";
        String text = aiLlmVisionClient.recognizeCaptcha(imageBase64, typeHint, length);
        text = sanitize(text, typeEnum, length);
        if (StringUtils.isBlank(text)) {
            throw new RecognitionException("AI识别验证码失败");
        }
        return text;
    }

    private String sanitize(String raw, CheckCodeTypeEnum typeEnum, int length) {
        if (raw == null) return "";
        String t = raw.replaceAll("[^A-Za-z0-9\\u4e00-\\u9fa5]", "").trim();
        if (typeEnum == CheckCodeTypeEnum.NUMBER) {
            t = t.replaceAll("[^0-9]", "");
        } else if (typeEnum == CheckCodeTypeEnum.ENGLISH) {
            t = t.replaceAll("[^A-Za-z]", "");
        } else if (typeEnum == CheckCodeTypeEnum.ENGLISH_WITH_NUMBER) {
            t = t.replaceAll("[^A-Za-z0-9]", "");
        } else if (typeEnum == CheckCodeTypeEnum.CHINESE) {
            t = t.replaceAll("[^\\u4e00-\\u9fa5]", "");
        }
        if (length > 0 && t.length() > length) {
            t = t.substring(0, length);
        }
        if (length > 0 && t.length() < length) {
            return "";
        }
        return t;
    }
}
