package cn.gdeiassistant.Repository.Redis.VerificationCode;

import cn.gdeiassistant.Tools.SpringUtils.RedisDaoUtils;
import cn.gdeiassistant.Tools.Utils.StringEncryptUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.concurrent.TimeUnit;

@Repository
public class VerificationCodeDaoImpl implements VerificationCodeDao {

    private final String PHONE_PREFIX = "PHONE_VERIFICATION_CODE_";
    private final String EMAIL_PREFIX = "EMAIL_VERIFICATION_CODE_";

    @Autowired
    private RedisDaoUtils redisDaoUtils;

    /**
     * 查询手机验证码记录
     *
     * @param code
     * @param phone
     * @return
     */
    public Integer QueryPhoneVerificationCode(int code, String phone) {
        return redisDaoUtils.get(StringEncryptUtils.SHA256HexString(PHONE_PREFIX + code + phone));
    }

    /**
     * 删除手机验证码记录
     *
     * @param code
     * @param phone
     */
    @Override
    public void DeletePhoneVerificationCode(int code, String phone) {
        redisDaoUtils.delete(StringEncryptUtils.SHA256HexString(PHONE_PREFIX + code + phone));
    }

    /**
     * 保存手机验证码记录
     *
     * @param code
     * @param phone
     * @param randomCode
     */
    public void SavePhoneVerificationCode(int code, String phone, int randomCode) {
        redisDaoUtils.set(StringEncryptUtils.SHA256HexString(PHONE_PREFIX + code + phone), randomCode);
        redisDaoUtils.expire(StringEncryptUtils.SHA256HexString(PHONE_PREFIX + code + phone), 5, TimeUnit.MINUTES);
    }

    /**
     * 查询电子邮件验证码记录
     * @param email
     * @return
     */
    public Integer QueryEmailVerificationCode(String email){
        return redisDaoUtils.get(StringEncryptUtils.SHA256HexString(EMAIL_PREFIX + email));
    }

    /**
     * 保存电子邮件验证码记录
     * @param email
     * @param randomCode
     */
    public void SaveEmailVerificationCode(String email, int randomCode){
        redisDaoUtils.set(StringEncryptUtils.SHA256HexString(EMAIL_PREFIX + email), randomCode);
        redisDaoUtils.expire(StringEncryptUtils.SHA256HexString(EMAIL_PREFIX + email), 5, TimeUnit.MINUTES);
    }

    /**
     * 删除电子邮件验证码记录
     * @param email
     */
    public void DeleteEmailVerificationCode(String email){
        redisDaoUtils.delete(StringEncryptUtils.SHA256HexString(EMAIL_PREFIX + email));
    }
}
