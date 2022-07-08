package cn.gdeiassistant.Service.OpenAPI.IPAddress;

import cn.gdeiassistant.Pojo.Entity.Location;
import cn.gdeiassistant.Tools.SpringUtils.JiSuAPIUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class IPAddressService {

    @Autowired
    private JiSuAPIUtils jiSuAPIUtils;

    /**
     * 根据IP地址查询IP地址归属地
     *
     * @param ip
     * @return
     */
    public Location GetLocationInfoByIPAddress(String ip) {
        return jiSuAPIUtils.GetLocationInfoByIPAddress(ip);
    }

}
