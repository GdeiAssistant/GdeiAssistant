package cn.gdeiassistant.core.email.service;

import cn.gdeiassistant.common.exception.VerificationException.SendEmailException;
import cn.gdeiassistant.common.exception.VerificationException.VerificationCodeInvalidException;
import cn.gdeiassistant.common.pojo.Entity.Email;
import cn.gdeiassistant.common.pojo.Entity.User;
import cn.gdeiassistant.common.redis.VerificationCode.VerificationCodeDao;
import cn.gdeiassistant.core.email.mapper.EmailMapper;
import cn.gdeiassistant.core.userLogin.service.UserCertificateService;
import cn.gdeiassistant.core.capability.email.EmailVerificationSender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class EmailService {

    @Autowired
    private EmailMapper emailMapper;

    @Autowired
    private VerificationCodeDao verificationCodeDao;

    @Autowired
    private UserCertificateService userCertificateService;


    @Autowired
    private EmailVerificationSender emailVerificationSender;

    /**
     * 查询用户绑定的电子邮箱地址信息
     *
     * @param sessionId
     * @return
     */
    public Email queryUserEmail(String sessionId) {
        User user = userCertificateService.getUserLoginCertificate(sessionId);
        return emailMapper.selectEmail(user.getUsername());
    }

    /**
     * 获取电子邮件验证码
     *
     * @param email
     */
    public void getEmailVerificationCode(String email) throws SendEmailException {
        //生成随机数
        int randomCode = (int) ((Math.random() * 9 + 1) * 100000);
        //写入Redis缓存记录
        verificationCodeDao.SaveEmailVerificationCode(email, randomCode);
        //发送电子邮件验证码
        try {
            emailVerificationSender.sendVerificationCode(email, randomCode);
        } catch (SendEmailException e) {
            verificationCodeDao.DeleteEmailVerificationCode(email);
            throw e;
        }
    }

    /**
     * 检测邮件验证码正确性
     *
     * @param email
     * @param randomCode
     */
    public void checkVerificationCode(String email, int randomCode) throws VerificationCodeInvalidException {
        Integer verificationCode = verificationCodeDao.QueryEmailVerificationCode(email);
        if (verificationCode != null) {
            if (verificationCode.equals(randomCode)) {
                //移除电子邮件验证码记录
                verificationCodeDao.DeleteEmailVerificationCode(email);
                //校验通过
                return;
            }
        }
        throw new VerificationCodeInvalidException();
    }

    /**
     * 添加或更新绑定的电子邮件地址信息
     *
     * @param sessionId
     * @param email
     */
    public void bindUserEmail(String sessionId, String email) {
        User user = userCertificateService.getUserLoginCertificate(sessionId);
        Email data = emailMapper.selectEmail(user.getUsername());
        if (data != null) {
            data.setEmail(email);
            emailMapper.updateEmail(data);
        } else {
            emailMapper.insertEmail(user.getUsername(),email);
        }
    }

    /**
     * 解除绑定用户的电子邮件地址信息
     *
     * @param sessionId
     */
    public void unBindUserEmail(String sessionId) {
        User user = userCertificateService.getUserLoginCertificate(sessionId);
        Email data = emailMapper.selectEmail(user.getUsername());
        if (data != null) {
            emailMapper.deleteEmail(user.getUsername());
        }
    }
}
