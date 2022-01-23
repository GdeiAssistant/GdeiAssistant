package cn.gdeiassistant.Repository.Redis.VerificationCode;

import cn.gdeiassistant.Tools.SpringUtils.RedisDaoUtils;
import cn.gdeiassistant.Tools.Utils.StringEncryptUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.concurrent.TimeUnit;

;

@Repository
public class VerificationCodeDaoImpl implements VerificationCodeDao {

    private final String PREFIX = "VERIFICATION_CODE_";

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
        return redisDaoUtils.get(StringEncryptUtils.SHA256HexString(PREFIX + code + phone));
    }

    /**
     * 删除手机验证码记录
     *
     * @param code
     * @param phone
     */
    @Override
    public void DeletePhoneVerificationCode(int code, String phone) {
        redisDaoUtils.delete(StringEncryptUtils.SHA256HexString(PREFIX + code + phone));
    }

    /**
     * 保存手机验证码记录
     *
     * @param code
     * @param phone
     * @param randomCode
     */
    public void SavePhoneVerificationCode(int code, String phone, int randomCode) {
        redisDaoUtils.set(StringEncryptUtils.SHA256HexString(PREFIX + code + phone), randomCode);
        redisDaoUtils.expire(StringEncryptUtils.SHA256HexString(PREFIX + code + phone), 5, TimeUnit.MINUTES);
    }
}
