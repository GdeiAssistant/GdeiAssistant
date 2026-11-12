package cn.gdeiassistant.common.spi;

import cn.gdeiassistant.common.pojo.entity.IPAddressRecord;

/**
 * Contract used by the login IP aspect. The ipaddress domain owns the implementation.
 */
public interface IPAddressRecorder {

    IPAddressRecord getInfoByIPAddress(String ip);

    void saveIPAddress(IPAddressRecord record);
}
