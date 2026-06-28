package cn.gdeiassistant.core.capability.impl;

import cn.gdeiassistant.common.exception.ProviderException;
import cn.gdeiassistant.common.tools.Utils.StringUtils;
import cn.gdeiassistant.core.capability.ServiceProvider;
import com.tencentcloudapi.common.Credential;
import com.tencentcloudapi.common.exception.TencentCloudSDKException;
import com.tencentcloudapi.common.profile.ClientProfile;
import com.tencentcloudapi.common.profile.HttpProfile;
import com.tencentcloudapi.sms.v20190711.SmsClient;
import com.tencentcloudapi.sms.v20190711.models.SendSmsRequest;
import com.tencentcloudapi.sms.v20190711.models.SendSmsResponse;
import com.tencentcloudapi.sms.v20190711.models.SendStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class TencentSmsProvider implements ServiceProvider<SmsRequest, Void> {

    private String secretId;
    private String secretKey;
    private String region;
    private String appId;
    private String signName;
    private String chinaTemplateId;
    private String globalTemplateId;

    @Value("${sms.tencent.secret-id:}")
    public void setSecretId(String secretId) {
        this.secretId = secretId;
    }

    @Value("${sms.tencent.secret-key:}")
    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    @Value("${sms.tencent.region:ap-guangzhou}")
    public void setRegion(String region) {
        this.region = region;
    }

    @Value("${sms.tencent.app-id:}")
    public void setAppId(String appId) {
        this.appId = appId;
    }

    @Value("${sms.tencent.sign-name:}")
    public void setSignName(String signName) {
        this.signName = signName;
    }

    @Value("${sms.tencent.template.china:}")
    public void setChinaTemplateId(String chinaTemplateId) {
        this.chinaTemplateId = chinaTemplateId;
    }

    @Value("${sms.tencent.template.global:}")
    public void setGlobalTemplateId(String globalTemplateId) {
        this.globalTemplateId = globalTemplateId;
    }

    @Override
    public String providerName() {
        return "tencent";
    }

    @Override
    public int priority() {
        return 0;
    }

    @Override
    public Void execute(SmsRequest request) throws ProviderException {
        String phoneE164 = request.isGlobal()
                ? request.getPhone()
                : "+86" + request.getPhone();
        String templateId = request.isGlobal()
                ? (StringUtils.isNotBlank(globalTemplateId) ? globalTemplateId : chinaTemplateId)
                : chinaTemplateId;

        if (StringUtils.isBlank(templateId)) {
            throw new ProviderException("腾讯云短信模板未配置");
        }

        try {
            Credential cred = new Credential(secretId, secretKey);
            HttpProfile httpProfile = new HttpProfile();
            httpProfile.setEndpoint("sms.tencentcloudapi.com");
            ClientProfile clientProfile = new ClientProfile();
            clientProfile.setHttpProfile(httpProfile);
            SmsClient client = new SmsClient(cred, region, clientProfile);

            SendSmsRequest req = new SendSmsRequest();
            req.setSmsSdkAppid(appId);
            req.setSign(signName);
            req.setTemplateID(templateId);
            req.setPhoneNumberSet(new String[]{phoneE164});
            req.setTemplateParamSet(new String[]{String.valueOf(request.getCode())});

            SendSmsResponse resp = client.SendSms(req);
            SendStatus[] statusSet = resp.getSendStatusSet();
            if (statusSet == null || statusSet.length == 0 || !"Ok".equalsIgnoreCase(statusSet[0].getCode())) {
                throw new ProviderException("腾讯云短信发送失败");
            }
            return null;
        } catch (TencentCloudSDKException e) {
            throw new ProviderException("腾讯云短信发送失败", e);
        }
    }

    @Override
    public boolean isConfigured() {
        return StringUtils.isNotBlank(secretId);
    }
}
