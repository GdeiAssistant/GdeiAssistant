package cn.gdeiassistant.core.capability.impl;

import cn.gdeiassistant.common.enums.Recognition.CheckCodeTypeEnum;

public class OcrRequest {

    private final String imageBase64;
    private final String prompt;
    private final CheckCodeTypeEnum typeEnum;
    private final int length;

    public OcrRequest(String imageBase64, String prompt, CheckCodeTypeEnum typeEnum, int length) {
        this.imageBase64 = imageBase64;
        this.prompt = prompt;
        this.typeEnum = typeEnum;
        this.length = length;
    }

    public static OcrRequest forCaptcha(String imageBase64, String typeHint, int length) {
        String prompt = "You are a captcha OCR engine. Read the captcha image and return ONLY the captcha text without spaces or punctuation."
                + " Type hint: " + typeHint + "."
                + (length > 0 ? " Expected length: " + length + "." : "")
                + " Output plain text only.";
        return new OcrRequest(imageBase64, prompt, null, length);
    }

    public static OcrRequest forDigits(String imageBase64) {
        String prompt = "Extract only digits from this image. Return digits only, no other text.";
        return new OcrRequest(imageBase64, prompt, null, 0);
    }

    public String getImageBase64() {
        return imageBase64;
    }

    public String getPrompt() {
        return prompt;
    }

    public CheckCodeTypeEnum getTypeEnum() {
        return typeEnum;
    }

    public int getLength() {
        return length;
    }
}
