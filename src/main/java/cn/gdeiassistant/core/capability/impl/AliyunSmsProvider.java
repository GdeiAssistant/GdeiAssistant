package cn.gdeiassistant.core.capability.impl;

import cn.gdeiassistant.common.exception.ProviderException;
import cn.gdeiassistant.common.exception.VerificationException.SendSMSException;
import cn.gdeiassistant.common.pojo.Config.AliYunSMSConfig;
import cn.gdeiassistant.common.tools.SpringUtils.AliYunSMSUtils;
import cn.gdeiassistant.common.tools.Utils.StringUtils;
import cn.gdeiassistant.core.capability.ServiceProvider;
import com.aliyuncs.exceptions.ClientException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AliyunSmsProvider implements ServiceProvider<SmsRequest, Void> {

    @Autowired
    private AliYunSMSUtils aliYunSMSUtils;

    @Autowired
    private AliYunSMSConfig aliyunSMSConfig;

    @Override
    public String providerName() {
        return "aliyun";
    }

    @Override
    public int priority() {
        return 1;
    }

    @Override
    public Void execute(SmsRequest request) throws ProviderException {
        try {
            if (request.isGlobal()) {
                aliYunSMSUtils.SendGlobalPhoneVerificationCodeSMS(request.getCode(), request.getAreaCode(), request.getPhone());
            } else {
                aliYunSMSUtils.SendChinaPhoneVerificationCodeSMS(request.getCode(), request.getPhone());
            }
            return null;
        } catch (ClientException | SendSMSException e) {
            throw new ProviderException("阿里云短信发送失败", e);
        }
    }

    @Override
    public boolean isConfigured() {
        return aliyunSMSConfig != null && StringUtils.isNotBlank(aliyunSMSConfig.getSmsAliyunAccessKeyId());
    }
}
