package cn.gdeiassistant.core.capability.impl;

import cn.gdeiassistant.common.pojo.Entity.IPAddressRecord;
import cn.gdeiassistant.core.capability.ip.IPLocationResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class FreeIpLocationResolver implements IPLocationResolver {

    @Autowired
    private IpLocationService ipLocationService;

    @Override
    public IPAddressRecord resolve(String ip) {
        return ipLocationService.resolve(ip);
    }
}
