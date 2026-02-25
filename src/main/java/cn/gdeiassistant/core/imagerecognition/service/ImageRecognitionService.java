package cn.gdeiassistant.core.imageRecognition.service;

import cn.gdeiassistant.common.enums.Recognition.CheckCodeTypeEnum;
import cn.gdeiassistant.common.exception.RecognitionException.RecognitionException;
import cn.gdeiassistant.common.tools.SpringUtils.AliYunAPIUtils;
import cn.gdeiassistant.common.tools.SpringUtils.JiSuAPIUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ImageRecognitionService {

    @Autowired
    private AliYunAPIUtils aliYunAPIUtils;

    @Autowired
    private JiSuAPIUtils jiSuAPIUtils;

    /**
     * 神经网络识别验证码图片，返回验证码
     *
     * @param image
     * @param checkCodeTypeEnum
     * @param length
     * @return
     */
    public String CheckCodeRecognize(String image, CheckCodeTypeEnum checkCodeTypeEnum, int length) throws RecognitionException {
        return jiSuAPIUtils.CheckCodeRecognize(image, checkCodeTypeEnum, length);
    }

    /**
     * OCR识别图片中的数字，返回数字文本串
     *
     * @param image
     * @return
     */
    public String CharacterNumberRecognize(String image) throws RecognitionException {
        return aliYunAPIUtils.CharacterNumberRecognize(image);
    }
}