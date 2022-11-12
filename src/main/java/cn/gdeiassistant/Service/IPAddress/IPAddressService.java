package cn.gdeiassistant.Service.OpenAPI.IPAddress;

import cn.gdeiassistant.Enum.IPAddress.IPAddressEnum;
import cn.gdeiassistant.Pojo.Entity.IPAddressRecord;
import cn.gdeiassistant.Pojo.Entity.Location;
import cn.gdeiassistant.Pojo.Entity.User;
import cn.gdeiassistant.Repository.SQL.Mysql.Mapper.GdeiAssistantLogs.IPAddress.IPAddressMapper;
import cn.gdeiassistant.Service.AccountManagement.UserLogin.UserCertificateService;
import cn.gdeiassistant.Tools.SpringUtils.JiSuAPIUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class IPAddressService {

    @Autowired
    private JiSuAPIUtils jiSuAPIUtils;

    @Autowired
    private IPAddressMapper ipAddressMapper;

    @Autowired
    private UserCertificateService userCertificateService;

    /**
     * 根据IP地址查询IP地址归属地
     *
     * @param ip
     * @return
     */
    public Location GetLocationInfoByIPAddress(String ip) {
        return jiSuAPIUtils.GetLocationInfoByIPAddress(ip);
    }

    /**
     * 查询其他用户最后的IP地址记录
     *
     * @param username
     * @return
     */
    public IPAddressRecord QueryOtherUserLatestPostTypeIPAddress(String username) {
        IPAddressRecord ipAddressRecord = ipAddressMapper.selectLatestIPAddressRecordByType(username, IPAddressEnum.POST.getValue());
        if (ipAddressRecord != null) {
            if (ipAddressRecord.getCountry().equals("中国")) {
                if (ipAddressRecord.getProvince() != null) {
                    ipAddressRecord.setArea(ipAddressRecord.getProvince());
                } else {
                    ipAddressRecord.setArea("-");
                }
            } else {
                ipAddressRecord.setArea(ipAddressRecord.getCountry());
            }
        } else {
            ipAddressRecord = new IPAddressRecord();
            ipAddressRecord.setArea("-");
        }
        return ipAddressRecord;
    }

    /**
     * 查询当前登录用户最后的IP地址记录
     *
     * @param sessionId
     * @return
     */
    public IPAddressRecord QuerySelfUserLatestPostTypeIPAddress(String sessionId) {
        User user = userCertificateService.GetUserLoginCertificate(sessionId);
        IPAddressRecord ipAddressRecord = QueryOtherUserLatestPostTypeIPAddress(user.getUsername());
        return ipAddressRecord;
    }

    /**
     * 保存IP地址记录
     *
     * @param sessionId
     * @param ipAddressRecord
     */
    public void SaveIPAddress(String sessionId, IPAddressRecord ipAddressRecord) {
        User user = userCertificateService.GetUserLoginCertificate(sessionId);
        if (user != null) {
            ipAddressRecord.setUsername(user.getUsername());
            ipAddressMapper.insertIPAddressRecord(ipAddressRecord);
        }
    }
}
