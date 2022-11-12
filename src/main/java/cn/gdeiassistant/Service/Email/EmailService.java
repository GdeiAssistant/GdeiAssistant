package cn.gdeiassistant.Service.AccountManagement.Email;

import cn.gdeiassistant.Exception.VerificationException.VerificationCodeInvalidException;
import cn.gdeiassistant.Pojo.Entity.Email;
import cn.gdeiassistant.Pojo.Entity.User;
import cn.gdeiassistant.Repository.Redis.VerificationCode.VerificationCodeDao;
import cn.gdeiassistant.Repository.SQL.Mysql.Mapper.GdeiAssistant.Email.EmailMapper;
import cn.gdeiassistant.Service.AccountManagement.UserLogin.UserCertificateService;
import cn.gdeiassistant.Service.OpenAPI.VerificationCode.VerificationCodeService;
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
    private VerificationCodeService verificationCodeService;

    /**
     * 查询用户绑定的电子邮箱地址信息
     *
     * @param sessionId
     * @return
     */
    public Email QueryUserEmail(String sessionId) {
        User user = userCertificateService.GetUserLoginCertificate(sessionId);
        return emailMapper.selectEmail(user.getUsername());
    }

    /**
     * 获取电子邮件验证码
     *
     * @param email
     */
    public void GetEmailVerificationCode(String email) {
        //生成随机数
        int randomCode = (int) ((Math.random() * 9 + 1) * 100000);
        //写入Redis缓存记录
        verificationCodeDao.SaveEmailVerificationCode(email, randomCode);
        //发送电子邮件验证码

        //TODO
    }

    /**
     * 检测邮件验证码正确性
     *
     * @param email
     * @param randomCode
     */
    public void CheckVerificationCode(String email, int randomCode) throws VerificationCodeInvalidException {
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
    public void BindUserEmail(String sessionId, String email) {
        User user = userCertificateService.GetUserLoginCertificate(sessionId);
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
    public void UnBindUserEmail(String sessionId) {
        User user = userCertificateService.GetUserLoginCertificate(sessionId);
        Email data = emailMapper.selectEmail(user.getUsername());
        if (data != null) {
            emailMapper.deleteEmail(user.getUsername());
        }
    }
}
