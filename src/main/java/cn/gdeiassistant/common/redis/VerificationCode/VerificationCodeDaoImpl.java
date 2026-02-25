package cn.gdeiassistant.common.redis.VerificationCode;

import cn.gdeiassistant.common.tools.SpringUtils.RedisDaoUtils;
import cn.gdeiassistant.common.tools.Utils.StringEncryptUtils;
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
        String s = redisDaoUtils.get(StringEncryptUtils.sha256HexString(PHONE_PREFIX + code + phone));
        return (s == null || s.isEmpty()) ? null : Integer.parseInt(s);
    }

    /**
     * 删除手机验证码记录
     *
     * @param code
     * @param phone
     */
    @Override
    public void DeletePhoneVerificationCode(int code, String phone) {
        redisDaoUtils.delete(StringEncryptUtils.sha256HexString(PHONE_PREFIX + code + phone));
    }

    /**
     * 保存手机验证码记录
     *
     * @param code
     * @param phone
     * @param randomCode
     */
    public void SavePhoneVerificationCode(int code, String phone, int randomCode) {
        String key = StringEncryptUtils.sha256HexString(PHONE_PREFIX + code + phone);
        redisDaoUtils.set(key, String.valueOf(randomCode));
        redisDaoUtils.expire(key, 5, TimeUnit.MINUTES);
    }

    /**
     * 查询电子邮件验证码记录
     * @param email
     * @return
     */
    public Integer QueryEmailVerificationCode(String email){
        String s = redisDaoUtils.get(StringEncryptUtils.sha256HexString(EMAIL_PREFIX + email));
        return (s == null || s.isEmpty()) ? null : Integer.parseInt(s);
    }

    public void SaveEmailVerificationCode(String email, int randomCode){
        String key = StringEncryptUtils.sha256HexString(EMAIL_PREFIX + email);
        redisDaoUtils.set(key, String.valueOf(randomCode));
        redisDaoUtils.expire(key, 5, TimeUnit.MINUTES);
    }

    /**
     * 删除电子邮件验证码记录
     * @param email
     */
    public void DeleteEmailVerificationCode(String email){
        redisDaoUtils.delete(StringEncryptUtils.sha256HexString(EMAIL_PREFIX + email));
    }
}
