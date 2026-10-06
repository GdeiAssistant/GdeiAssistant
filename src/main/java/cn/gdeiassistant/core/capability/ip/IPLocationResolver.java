package cn.gdeiassistant.core.capability.ip;

import cn.gdeiassistant.common.pojo.entity.IPAddressRecord;

public interface IPLocationResolver {

    IPAddressRecord resolve(String ip);
}
