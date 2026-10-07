package cn.gdeiassistant.core.phone.service;

import cn.gdeiassistant.common.exception.verificationexception.SendSMSException;
import cn.gdeiassistant.common.exception.verificationexception.VerificationCodeInvalidException;
import cn.gdeiassistant.common.pojo.entity.User;
import cn.gdeiassistant.common.redis.verificationcode.VerificationCodeDao;
import cn.gdeiassistant.core.phone.mapper.PhoneMapper;
import cn.gdeiassistant.core.phone.pojo.dto.PhoneBindDTO;
import cn.gdeiassistant.core.phone.pojo.entity.PhoneEntity;
import cn.gdeiassistant.core.phone.pojo.vo.PhoneVO;
import cn.gdeiassistant.core.userlogin.service.UserCertificateService;
import cn.gdeiassistant.core.verificationcode.service.VerificationCodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PhoneService {

    @Autowired
    private PhoneMapper phoneMapper;

    @Autowired
    private VerificationCodeDao verificationCodeDao;

    @Autowired
    private UserCertificateService userCertificateService;

    @Autowired
    private VerificationCodeService verificationCodeService;

    /**
     * 查询用户绑定的手机号信息（返回 VO，手机号脱敏）
     */
    public PhoneVO queryUserPhone(String sessionId) {
        User user = userCertificateService.getUserLoginCertificate(sessionId);
        PhoneEntity entity = phoneMapper.selectPhone(user.getUsername());
        if (entity == null) return null;
        PhoneVO vo = new PhoneVO();
        vo.setUsername(entity.getUsername());
        String raw = entity.getPhone() != null ? entity.getPhone().toString() : "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < raw.length(); i++) {
            sb.append(i < 3 ? raw.charAt(i) : '*');
        }
        vo.setPhone(sb.toString());
        return vo;
    }

    /**
     * 获取手机验证码
     *
     * @param code
     * @param phone
     */
    public void getPhoneVerificationCode(int code, String phone) throws SendSMSException {
        //生成随机数
        int randomCode = 100000 + new java.security.SecureRandom().nextInt(900000);
        //写入Redis缓存记录
        verificationCodeDao.savePhoneVerificationCode(code, phone, randomCode);
        try {
        if (code == 86) {
            //国内手机号
            verificationCodeService.sendChinaPhoneVerificationCodeSms(randomCode, phone);
        } else {
            //国际/港澳台手机号
            verificationCodeService.sendGlobalPhoneVerificationCodeSms(randomCode, code, phone);
        }
        } catch (SendSMSException e) {
            verificationCodeDao.consumePhoneVerificationCode(code, phone, randomCode);
            throw e;
        }
    }

    /**
     * 检测手机验证码正确性
     *
     * @param code
     * @param phone
     * @param randomCode
     */
    public void checkVerificationCode(int code, String phone, int randomCode) throws VerificationCodeInvalidException {
        if (verificationCodeDao.consumePhoneVerificationCode(code, phone, randomCode)) return;
        throw new VerificationCodeInvalidException();
    }

    /**
     * 添加或更新绑定的手机号信息
     */
    public void attachUserPhone(String sessionId, PhoneBindDTO dto) {
        User user = userCertificateService.getUserLoginCertificate(sessionId);
        PhoneEntity entity = phoneMapper.selectPhone(user.getUsername());
        if (entity != null) {
            entity.setCode(dto.getCode());
            entity.setPhone(dto.getPhone());
            phoneMapper.updatePhone(entity);
        } else {
            phoneMapper.insertPhone(user.getUsername(), dto.getCode(), dto.getPhone());
        }
    }

    /**
     * 解除绑定用户的手机号信息
     */
    public void unAttachUserPhone(String sessionId) {
        User user = userCertificateService.getUserLoginCertificate(sessionId);
        PhoneEntity entity = phoneMapper.selectPhone(user.getUsername());
        if (entity != null) {
            phoneMapper.deletePhone(user.getUsername());
        }
    }
}
