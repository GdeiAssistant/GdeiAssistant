package cn.gdeiassistant.Service.Recognition;

import cn.gdeiassistant.Enum.Recognition.CheckCodeTypeEnum;
import cn.gdeiassistant.Exception.RecognitionException.RecognitionException;
import cn.gdeiassistant.Service.CloudAPI.AliYunService;
import cn.gdeiassistant.Service.CloudAPI.JiSuAPIService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RecognitionService {

    @Autowired
    private AliYunService aliyunService;

    @Autowired
    private JiSuAPIService jiSuAPIService;

    /**
     * 神经网络识别验证码图片，返回验证码
     *
     * @param image
     * @param checkCodeTypeEnum
     * @param length
     * @return
     */
    public String CheckCodeRecognize(String image, CheckCodeTypeEnum checkCodeTypeEnum, int length) throws RecognitionException {
        return jiSuAPIService.CheckCodeRecognize(image, checkCodeTypeEnum, length);
    }

    /**
     * OCR识别图片中的数字，返回数字文本串
     *
     * @param image
     * @return
     */
    public String CharacterNumberRecognize(String image) throws RecognitionException {
        return aliyunService.CharacterNumberRecognize(image);
    }
}