package cn.gdeiassistant.core.capability.impl;

import cn.gdeiassistant.common.exception.ProviderChainExhaustedException;
import cn.gdeiassistant.common.exception.VerificationException.SendSMSException;
import cn.gdeiassistant.core.capability.ProviderChain;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SmsService {

    private static final Logger log = LoggerFactory.getLogger(SmsService.class);

    private final ProviderChain<SmsRequest, Void> chain;

    @Autowired
    public SmsService(TencentSmsProvider tencent,
                      AliyunSmsProvider aliyun,
                      MeterRegistry meterRegistry,
                      CircuitBreakerRegistry circuitBreakerRegistry) {
        this.chain = new ProviderChain<>(
                "sms",
                List.of(tencent, aliyun),
                meterRegistry,
                circuitBreakerRegistry
        );
    }

    public void sendChina(int code, String phone) throws SendSMSException {
        try {
            chain.execute(new SmsRequest(code, phone, false));
        } catch (ProviderChainExhaustedException e) {
            log.error("所有短信服务商均不可用：{}", e.getMessage());
            throw new SendSMSException("短信发送失败，所有服务商均不可用");
        }
    }

    public void sendGlobal(int code, int areaCode, String phone) throws SendSMSException {
        try {
            chain.execute(new SmsRequest(code, phone, true, areaCode));
        } catch (ProviderChainExhaustedException e) {
            log.error("所有短信服务商均不可用：{}", e.getMessage());
            throw new SendSMSException("短信发送失败，所有服务商均不可用");
        }
    }

    public ProviderChain<SmsRequest, Void> getChain() {
        return chain;
    }
}
