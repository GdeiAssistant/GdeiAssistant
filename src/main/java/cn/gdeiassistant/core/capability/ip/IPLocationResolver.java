package cn.gdeiassistant.core.capability.ip;

import cn.gdeiassistant.common.pojo.Entity.IPAddressRecord;

public interface IPLocationResolver {

    IPAddressRecord resolve(String ip);
}
