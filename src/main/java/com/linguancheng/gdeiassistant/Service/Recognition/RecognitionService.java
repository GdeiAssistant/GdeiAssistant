package com.gdeiassistant.gdeiassistant.Service.Recognition;

import com.gdeiassistant.gdeiassistant.Enum.Recognition.CheckCodeTypeEnum;
import com.gdeiassistant.gdeiassistant.Enum.Recognition.RecognitionEnum;
import com.gdeiassistant.gdeiassistant.Exception.RecognitionException.RecognitionException;
import com.gdeiassistant.gdeiassistant.Pojo.Entity.Identity;
import com.gdeiassistant.gdeiassistant.Service.CloudAPI.AliYunService;
import com.gdeiassistant.gdeiassistant.Service.CloudAPI.BaiduYunService;
import com.gdeiassistant.gdeiassistant.Service.CloudAPI.JiSuAPIService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RecognitionService {

    @Autowired
    private AliYunService aliyunService;

    @Autowired
    private BaiduYunService baiduYunService;

    @Autowired
    private JiSuAPIService jiSuAPIService;

    /**
     * 注册人脸库信息
     *
     * @param username
     * @param image
     * @return
     * @throws RecognitionException
     */
    public RecognitionEnum FaceSetRegister(String username, String image) throws RecognitionException {
        return baiduYunService.FaceSetRegister(username, image);
    }

    /**
     * 人脸认证
     *
     * @param username
     * @param image
     * @return
     * @throws RecognitionException
     */
    public RecognitionEnum FaceVerify(String username, String image) throws RecognitionException {
        return baiduYunService.FaceVerify(username, image);
    }

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
        return baiduYunService.CharacterNumberRecognize(image);
    }

    /**
     * OCR高精度识别验证码图片的文字，返回文本串
     *
     * @param image
     * @return
     * @throws RecognitionException
     */
    public String AccurateCharacterRecognize(String image) throws RecognitionException {
        return baiduYunService.AccurateCharacterRecognize(image);
    }

    /**
     * 识别身份证图片的文字，解析身份证照片信息
     *
     * @param image
     * @return
     * @throws Exception
     */
    public Identity ParseIdentityCardInfo(String image) throws Exception {
        return baiduYunService.ParseIdentityCard(image);
    }
}
