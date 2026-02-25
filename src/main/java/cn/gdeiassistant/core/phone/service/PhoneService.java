package cn.gdeiassistant.core.phone.service;

import cn.gdeiassistant.common.exception.VerificationException.SendSMSException;
import cn.gdeiassistant.common.exception.VerificationException.VerificationCodeInvalidException;
import cn.gdeiassistant.common.pojo.Entity.User;
import cn.gdeiassistant.common.redis.VerificationCode.VerificationCodeDao;
import cn.gdeiassistant.core.phone.mapper.PhoneMapper;
import cn.gdeiassistant.core.phone.pojo.dto.PhoneBindDTO;
import cn.gdeiassistant.core.phone.pojo.entity.PhoneEntity;
import cn.gdeiassistant.core.phone.pojo.vo.PhoneVO;
import cn.gdeiassistant.core.userLogin.service.UserCertificateService;
import cn.gdeiassistant.core.verificationCode.service.VerificationCodeService;
import com.aliyuncs.exceptions.ClientException;
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
    public void getPhoneVerificationCode(int code, String phone) throws ClientException, SendSMSException {
        //生成随机数
        int randomCode = (int) ((Math.random() * 9 + 1) * 100000);
        //写入Redis缓存记录
        verificationCodeDao.SavePhoneVerificationCode(code, phone, randomCode);
        if (code == 86) {
            //国内手机号
            verificationCodeService.sendChinaPhoneVerificationCodeSms(randomCode, phone);
        } else {
            //国际/港澳台手机号
            verificationCodeService.sendGlobalPhoneVerificationCodeSms(randomCode, code, phone);
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
        Integer verificationCode = verificationCodeDao.QueryPhoneVerificationCode(code, phone);
        if (verificationCode != null) {
            if (verificationCode.equals(randomCode)) {
                //移除手机验证码记录
                verificationCodeDao.DeletePhoneVerificationCode(code, phone);
                //校验通过
                return;
            }
        }
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
