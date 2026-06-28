package cn.gdeiassistant.core.capability.impl;

import cn.gdeiassistant.common.exception.ProviderChainExhaustedException;
import cn.gdeiassistant.common.pojo.Entity.IPAddressRecord;
import cn.gdeiassistant.core.capability.ProviderChain;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class IpLocationService {

    private static final Logger log = LoggerFactory.getLogger(IpLocationService.class);

    private final ProviderChain<String, IPAddressRecord> chain;

    @Autowired
    public IpLocationService(MaxmindGeoIpProvider maxmindGeoIpProvider,
                             IpApiProvider ipApiProvider,
                             IpWhoisProvider ipWhoisProvider,
                             MeterRegistry meterRegistry,
                             CircuitBreakerRegistry circuitBreakerRegistry) {
        this.chain = new ProviderChain<>(
                "ip-location",
                List.of(maxmindGeoIpProvider, ipApiProvider, ipWhoisProvider),
                meterRegistry,
                circuitBreakerRegistry
        );
    }

    public IPAddressRecord resolve(String ip) {
        try {
            return chain.execute(ip);
        } catch (ProviderChainExhaustedException e) {
            log.error("所有 IP 定位服务商均不可用：{}", e.getMessage());
            return fallbackRecord();
        }
    }

    public ProviderChain<String, IPAddressRecord> getChain() {
        return chain;
    }

    private IPAddressRecord fallbackRecord() {
        IPAddressRecord record = new IPAddressRecord();
        record.setCountry("-");
        record.setProvince("-");
        record.setCity("-");
        record.setNetwork("-");
        record.setArea("-");
        return record;
    }
}
