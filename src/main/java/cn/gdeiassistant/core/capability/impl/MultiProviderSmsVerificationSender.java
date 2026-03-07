package cn.gdeiassistant.core.capability.impl;

import cn.gdeiassistant.common.exception.VerificationException.SendSMSException;
import cn.gdeiassistant.common.tools.SpringUtils.AliYunSMSUtils;
import cn.gdeiassistant.common.tools.Utils.StringUtils;
import cn.gdeiassistant.core.capability.sms.SmsVerificationSender;
import com.aliyuncs.exceptions.ClientException;
import com.tencentcloudapi.common.Credential;
import com.tencentcloudapi.common.exception.TencentCloudSDKException;
import com.tencentcloudapi.common.profile.ClientProfile;
import com.tencentcloudapi.common.profile.HttpProfile;
import com.tencentcloudapi.sms.v20210111.SmsClient;
import com.tencentcloudapi.sms.v20210111.models.SendSmsRequest;
import com.tencentcloudapi.sms.v20210111.models.SendSmsResponse;
import com.tencentcloudapi.sms.v20210111.models.SendStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;

@Component
public class MultiProviderSmsVerificationSender implements SmsVerificationSender {


    @Autowired(required = false)
    private AliYunSMSUtils aliYunSMSUtils;

    @Autowired
    private RestTemplate restTemplate;

    private String provider;

    private String tencentSecretId;
    private String tencentSecretKey;
    private String tencentRegion;
    private String tencentAppId;
    private String tencentSignName;
    private String tencentChinaTemplateId;
    private String tencentGlobalTemplateId;

    private String huaweiAppKey;
    private String huaweiAppSecret;
    private String huaweiSender;
    private String huaweiSignature;
    private String huaweiChinaTemplateId;
    private String huaweiGlobalTemplateId;
    private String huaweiEndpoint;

    @Value("${sms.provider:tencent}")
    public void setProvider(String provider) {
        this.provider = provider;
    }

    @Value("${sms.tencent.secret-id:}")
    public void setTencentSecretId(String tencentSecretId) {
        this.tencentSecretId = tencentSecretId;
    }

    @Value("${sms.tencent.secret-key:}")
    public void setTencentSecretKey(String tencentSecretKey) {
        this.tencentSecretKey = tencentSecretKey;
    }

    @Value("${sms.tencent.region:ap-guangzhou}")
    public void setTencentRegion(String tencentRegion) {
        this.tencentRegion = tencentRegion;
    }

    @Value("${sms.tencent.app-id:}")
    public void setTencentAppId(String tencentAppId) {
        this.tencentAppId = tencentAppId;
    }

    @Value("${sms.tencent.sign-name:}")
    public void setTencentSignName(String tencentSignName) {
        this.tencentSignName = tencentSignName;
    }

    @Value("${sms.tencent.template.china:}")
    public void setTencentChinaTemplateId(String tencentChinaTemplateId) {
        this.tencentChinaTemplateId = tencentChinaTemplateId;
    }

    @Value("${sms.tencent.template.global:}")
    public void setTencentGlobalTemplateId(String tencentGlobalTemplateId) {
        this.tencentGlobalTemplateId = tencentGlobalTemplateId;
    }

    @Value("${sms.huawei.app-key:}")
    public void setHuaweiAppKey(String huaweiAppKey) {
        this.huaweiAppKey = huaweiAppKey;
    }

    @Value("${sms.huawei.app-secret:}")
    public void setHuaweiAppSecret(String huaweiAppSecret) {
        this.huaweiAppSecret = huaweiAppSecret;
    }

    @Value("${sms.huawei.sender:}")
    public void setHuaweiSender(String huaweiSender) {
        this.huaweiSender = huaweiSender;
    }

    @Value("${sms.huawei.signature:}")
    public void setHuaweiSignature(String huaweiSignature) {
        this.huaweiSignature = huaweiSignature;
    }

    @Value("${sms.huawei.template.china:}")
    public void setHuaweiChinaTemplateId(String huaweiChinaTemplateId) {
        this.huaweiChinaTemplateId = huaweiChinaTemplateId;
    }

    @Value("${sms.huawei.template.global:}")
    public void setHuaweiGlobalTemplateId(String huaweiGlobalTemplateId) {
        this.huaweiGlobalTemplateId = huaweiGlobalTemplateId;
    }

    @Value("${sms.huawei.endpoint:https://smsapi.cn-north-4.myhuaweicloud.com:443}")
    public void setHuaweiEndpoint(String huaweiEndpoint) {
        this.huaweiEndpoint = huaweiEndpoint;
    }

    @Override
    public void sendChina(int code, String phone) throws SendSMSException {
        String selected = normalizedProvider();
        switch (selected) {
            case "tencent":
                sendViaTencent("+86" + phone, tencentChinaTemplateId, code);
                return;
            case "aliyun":
                sendViaAliYunChina(code, phone);
                return;
            case "huawei":
                sendViaHuawei("86" + phone, huaweiChinaTemplateId, code);
                return;
            default:
                throw new SendSMSException("短信服务商配置不合法：sms.provider 仅支持 tencent / aliyun / huawei");
        }
    }

    @Override
    public void sendGlobal(int code, int areaCode, String phone) throws SendSMSException {
        String selected = normalizedProvider();
        switch (selected) {
            case "tencent":
                String template = StringUtils.isNotBlank(tencentGlobalTemplateId) ? tencentGlobalTemplateId : tencentChinaTemplateId;
                sendViaTencent("+" + areaCode + phone, template, code);
                return;
            case "aliyun":
                sendViaAliYunGlobal(code, areaCode, phone);
                return;
            case "huawei":
                String huaweiTemplate = StringUtils.isNotBlank(huaweiGlobalTemplateId) ? huaweiGlobalTemplateId : huaweiChinaTemplateId;
                sendViaHuawei(areaCode + phone, huaweiTemplate, code);
                return;
            default:
                throw new SendSMSException("短信服务商配置不合法：sms.provider 仅支持 tencent / aliyun / huawei");
        }
    }

    private void sendViaAliYunChina(int code, String phone) throws SendSMSException {
        if (aliYunSMSUtils == null) {
            throw new SendSMSException("短信功能未启用：Aliyun 发送器未注入");
        }
        try {
            aliYunSMSUtils.SendChinaPhoneVerificationCodeSMS(code, phone);
        } catch (ClientException e) {
            throw new SendSMSException("阿里云短信发送失败");
        }
    }

    private void sendViaAliYunGlobal(int code, int areaCode, String phone) throws SendSMSException {
        if (aliYunSMSUtils == null) {
            throw new SendSMSException("短信功能未启用：Aliyun 发送器未注入");
        }
        try {
            aliYunSMSUtils.SendGlobalPhoneVerificationCodeSMS(code, areaCode, phone);
        } catch (ClientException e) {
            throw new SendSMSException("阿里云短信发送失败");
        }
    }

    private void sendViaTencent(String phoneE164, String templateId, int code) throws SendSMSException {
        if (!isTencentConfigured() || StringUtils.isBlank(templateId)) {
            throw new SendSMSException("短信功能未启用：请配置 sms.tencent.*");
        }
        try {
            Credential cred = new Credential(tencentSecretId, tencentSecretKey);
            HttpProfile httpProfile = new HttpProfile();
            httpProfile.setEndpoint("sms.tencentcloudapi.com");
            ClientProfile clientProfile = new ClientProfile();
            clientProfile.setHttpProfile(httpProfile);
            SmsClient client = new SmsClient(cred, tencentRegion, clientProfile);

            SendSmsRequest req = new SendSmsRequest();
            req.setSmsSdkAppId(tencentAppId);
            req.setSignName(tencentSignName);
            req.setTemplateId(templateId);
            req.setPhoneNumberSet(new String[]{phoneE164});
            req.setTemplateParamSet(new String[]{String.valueOf(code)});

            SendSmsResponse resp = client.SendSms(req);
            SendStatus[] statusSet = resp.getSendStatusSet();
            if (statusSet == null || statusSet.length == 0 || !"Ok".equalsIgnoreCase(statusSet[0].getCode())) {
                throw new SendSMSException("腾讯云短信发送失败");
            }
        } catch (TencentCloudSDKException e) {
            throw new SendSMSException("腾讯云短信发送失败");
        }
    }

    private void sendViaHuawei(String phone, String templateId, int code) throws SendSMSException {
        if (!isHuaweiConfigured() || StringUtils.isBlank(templateId)) {
            throw new SendSMSException("短信功能未启用：请配置 sms.huawei.*");
        }
        try {
            String created = OffsetDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
            byte[] nonceBytes = new byte[16];
            new SecureRandom().nextBytes(nonceBytes);
            String nonceBase64 = Base64.getEncoder().encodeToString(nonceBytes);

            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(nonceBytes);
            md.update(created.getBytes(StandardCharsets.UTF_8));
            md.update(huaweiAppSecret.getBytes(StandardCharsets.UTF_8));
            String passwordDigest = Base64.getEncoder().encodeToString(md.digest());

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            headers.set("Authorization", "WSSE realm=\"SDP\",profile=\"UsernameToken\",type=\"Appkey\"");
            headers.set("X-WSSE", "UsernameToken Username=\"" + huaweiAppKey
                    + "\",PasswordDigest=\"" + passwordDigest
                    + "\",Nonce=\"" + nonceBase64
                    + "\",Created=\"" + created + "\"");

            LinkedMultiValueMap<String, String> form = new LinkedMultiValueMap<>();
            form.add("from", huaweiSender);
            form.add("to", phone);
            form.add("templateId", templateId);
            form.add("templateParas", "[\"" + code + "\"]");
            form.add("signature", huaweiSignature);

            String url = huaweiEndpoint + "/sms/batchSendSms/v1";
            String res = restTemplate.postForObject(url, new HttpEntity<>(form, headers), String.class);
            // 华为云以 code=000000 表示受理成功，其他 code 统一视为失败
            if (res == null || !res.contains("\"code\":\"000000\"")) {
                throw new SendSMSException("华为云短信发送失败");
            }
        } catch (SendSMSException e) {
            throw e;
        } catch (Exception e) {
            throw new SendSMSException("华为云短信发送失败");
        }
    }

    private String normalizedProvider() {
        return StringUtils.isNotBlank(provider) ? provider.trim().toLowerCase() : "tencent";
    }

    private boolean isTencentConfigured() {
        return StringUtils.isNotBlank(tencentSecretId)
                && StringUtils.isNotBlank(tencentSecretKey)
                && StringUtils.isNotBlank(tencentAppId)
                && StringUtils.isNotBlank(tencentSignName)
                && StringUtils.isNotBlank(tencentChinaTemplateId);
    }

    private boolean isHuaweiConfigured() {
        return StringUtils.isNotBlank(huaweiAppKey)
                && StringUtils.isNotBlank(huaweiAppSecret)
                && StringUtils.isNotBlank(huaweiSender)
                && StringUtils.isNotBlank(huaweiSignature)
                && StringUtils.isNotBlank(huaweiChinaTemplateId)
                && StringUtils.isNotBlank(huaweiEndpoint);
    }
}
