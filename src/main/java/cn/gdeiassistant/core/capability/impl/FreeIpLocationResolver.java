package cn.gdeiassistant.core.capability.impl;

import cn.gdeiassistant.common.pojo.entity.IPAddressRecord;
import cn.gdeiassistant.core.capability.ip.IPLocationResolver;
import org.springframework.stereotype.Component;

@Component
public class FreeIpLocationResolver implements IPLocationResolver {

    private final IpLocationService ipLocationService;

    public FreeIpLocationResolver(IpLocationService ipLocationService) {
        this.ipLocationService = ipLocationService;
    }

    @Override
    public IPAddressRecord resolve(String ip) {
        return ipLocationService.resolve(ip);
    }
}
