package edu.gdei.gdeiassistant.Repository.Redis.VerificationCode;

import edu.gdei.gdeiassistant.Tools.StringEncryptUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.concurrent.TimeUnit;

@Repository
public class VerificationCodeDaoImpl implements VerificationCodeDao {

    private final String PREFIX = "VERIFICATION_CODE_";

    @Autowired
    private RedisTemplate<String, Integer> redisTemplate;

    /**
     * 查询手机验证码记录
     *
     * @param code
     * @param phone
     * @return
     */
    public Integer QueryPhoneVerificationCode(int code, String phone) {
        return redisTemplate.opsForValue().get(StringEncryptUtils.SHA256HexString(PREFIX + code + phone));
    }

    /**
     * 删除手机验证码记录
     *
     * @param code
     * @param phone
     */
    @Override
    public void DeletePhoneVerificationCode(int code, String phone) {
        redisTemplate.delete(StringEncryptUtils.SHA256HexString(PREFIX + code + phone));
    }

    /**
     * 保存手机验证码记录
     *
     * @param code
     * @param phone
     * @param randomCode
     */
    public void SavePhoneVerificationCode(int code, String phone, int randomCode) {
        redisTemplate.opsForValue().set(StringEncryptUtils.SHA256HexString(PREFIX + code + phone), randomCode);
        //设置有效期为5分钟
        redisTemplate.expire(StringEncryptUtils.SHA256HexString(PREFIX + code + phone), 5, TimeUnit.MINUTES);
    }
}
