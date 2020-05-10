package edu.gdei.gdeiassistant.Service.Phone;

import com.aliyuncs.exceptions.ClientException;
import com.taobao.wsgsvr.WsgException;
import edu.gdei.gdeiassistant.Exception.PhoneException.SendSMSException;
import edu.gdei.gdeiassistant.Exception.PhoneException.VerificationCodeInvalidException;
import edu.gdei.gdeiassistant.Pojo.Entity.Phone;
import edu.gdei.gdeiassistant.Repository.SQL.Mysql.Mapper.GdeiAssistant.Phone.PhoneMapper;
import edu.gdei.gdeiassistant.Repository.Redis.VerificationCode.VerificationCodeDao;
import edu.gdei.gdeiassistant.Service.CloudAPI.AliYunService;
import edu.gdei.gdeiassistant.Tools.StringEncryptUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PhoneService {

    @Autowired
    private PhoneMapper phoneMapper;

    @Autowired
    private VerificationCodeDao verificationCodeDao;

    @Autowired
    private AliYunService aliYunService;

    /**
     * 查询用户绑定的手机号信息
     *
     * @param username
     * @return
     */
    public Phone QueryUserPhone(String username) throws WsgException {
        Phone phone = phoneMapper.selectPhone(StringEncryptUtils.encryptString(username));
        if (phone != null) {
            //解密手机号
            phone.setPhone(StringEncryptUtils.decryptString(phone.getPhone()));
            //隐藏用户绑定的手机号
            StringBuilder stringBuilder = new StringBuilder();
            for (int i = 0; i < phone.getPhone().length(); i++) {
                if (i < 3) {
                    stringBuilder.append(phone.getPhone().charAt(i));
                } else {
                    stringBuilder.append('*');
                }
            }
            phone.setPhone(stringBuilder.toString());
        }
        return phone;
    }

    /**
     * 获取手机验证码
     *
     * @param code
     * @param phone
     */
    public void GetPhoneVerificationCode(int code, String phone) throws ClientException, SendSMSException {
        //生成随机数
        int randomCode = (int) ((Math.random() * 9 + 1) * 100000);
        //写入Redis缓存记录
        verificationCodeDao.SavePhoneVerificationCode(code, phone, randomCode);
        if (code == 86) {
            //国内手机号
            aliYunService.SendChinaPhoneVerificationCodeSMS(randomCode, phone);
        } else {
            //国际/港澳台手机号
            aliYunService.SendGlobalPhoneVerificationCodeSMS(randomCode, code, phone);
        }
    }

    /**
     * 检测手机验证码正确性
     *
     * @param code
     * @param phone
     * @param randomCode
     */
    public void CheckVerificationCode(int code, String phone, int randomCode) throws VerificationCodeInvalidException {
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
     *
     * @param username
     * @param code
     * @param phone
     */
    public void AttachUserPhone(String username, Integer code, String phone) throws WsgException {
        Phone data = phoneMapper.selectPhone(StringEncryptUtils.encryptString(username));
        if (data != null) {
            data.setCode(code);
            data.setPhone(StringEncryptUtils.encryptString(phone));
            phoneMapper.updatePhone(data);
        } else {
            phoneMapper.insertPhone(StringEncryptUtils.encryptString(username), code, StringEncryptUtils.encryptString(phone));
        }
    }

    /**
     * 解除绑定用户的手机号信息
     *
     * @param username
     * @throws WsgException
     */
    public void UnAttachUserPhone(String username) throws WsgException {
        Phone data = phoneMapper.selectPhone(StringEncryptUtils.encryptString(username));
        if (data != null) {
            phoneMapper.deletePhone(StringEncryptUtils.encryptString(username));
        }
    }
}
