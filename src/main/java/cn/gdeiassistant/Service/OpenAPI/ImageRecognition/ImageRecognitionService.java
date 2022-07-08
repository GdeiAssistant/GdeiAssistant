package cn.gdeiassistant.Service.OpenAPI.ImageRecognition;

import cn.gdeiassistant.Enum.Recognition.CheckCodeTypeEnum;
import cn.gdeiassistant.Exception.RecognitionException.RecognitionException;
import cn.gdeiassistant.Tools.SpringUtils.AliYunAPIUtils;
import cn.gdeiassistant.Tools.SpringUtils.JiSuAPIUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ImageRecognitionService {

    @Autowired
    private AliYunAPIUtils aliyunAPIUtils;

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
        return aliyunAPIUtils.CharacterNumberRecognize(image);
    }
}