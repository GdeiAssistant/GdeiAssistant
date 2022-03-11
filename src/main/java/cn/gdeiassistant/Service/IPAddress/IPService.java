package cn.gdeiassistant.Service.IPAddress;

import cn.gdeiassistant.Pojo.Entity.Location;
import cn.gdeiassistant.Service.CloudAPI.JiSuAPIService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class IPService {

    @Autowired
    private JiSuAPIService jiSuAPIService;

    /**
     * 根据IP地址查询IP地址归属地
     *
     * @param ip
     * @return
     */
    public Location GetLocationInfoByIPAddress(String ip) {
        return jiSuAPIService.GetLocationInfoByIPAddress(ip);
    }

}
