package com.linguancheng.gdeiassistant.Service.Recognition;

import com.linguancheng.gdeiassistant.Enum.Recognition.CheckCodeTypeEnum;
import com.linguancheng.gdeiassistant.Enum.Recognition.RecognitionEnum;
import com.linguancheng.gdeiassistant.Exception.RecognitionException.RecognitionException;
import com.linguancheng.gdeiassistant.Pojo.Entity.Identity;
import com.linguancheng.gdeiassistant.Service.CloudAPI.AliYunService;
import com.linguancheng.gdeiassistant.Service.CloudAPI.BaiduYunService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RecognitionService {

    @Autowired
    private AliYunService aliyunService;

    @Autowired
    private BaiduYunService baiduYunService;

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
        return aliyunService.CheckCodeRecognize(image, checkCodeTypeEnum, length);
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
