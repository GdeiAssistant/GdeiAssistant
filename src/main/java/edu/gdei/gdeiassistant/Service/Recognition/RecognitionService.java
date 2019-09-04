package edu.gdei.gdeiassistant.Service.Recognition;

import edu.gdei.gdeiassistant.Enum.Recognition.CheckCodeTypeEnum;
import edu.gdei.gdeiassistant.Exception.RecognitionException.RecognitionException;
import edu.gdei.gdeiassistant.Pojo.Entity.Identity;
import edu.gdei.gdeiassistant.Service.CloudAPI.AliYunService;
import edu.gdei.gdeiassistant.Service.CloudAPI.JiSuAPIService;
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

    /**
     * 识别身份证图片的文字，解析身份证照片信息
     *
     * @param image
     * @return
     * @throws Exception
     */
    public Identity ParseIdentityCardInfo(String image) throws Exception {
        return aliyunService.ParseIdentityCard(image);
    }
}