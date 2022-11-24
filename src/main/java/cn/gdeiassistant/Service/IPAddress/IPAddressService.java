package cn.gdeiassistant.Service.IPAddress;

import cn.gdeiassistant.Enum.IPAddress.IPAddressEnum;
import cn.gdeiassistant.Pojo.Entity.IPAddressRecord;
import cn.gdeiassistant.Pojo.Entity.User;
import cn.gdeiassistant.Repository.SQL.Mysql.Mapper.GdeiAssistantLogs.IPAddress.IPAddressMapper;
import cn.gdeiassistant.Service.UserLogin.UserCertificateService;
import cn.gdeiassistant.Tools.SpringUtils.JiSuAPIUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

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
    public IPAddressRecord GetInfoByIPAddress(String ip) {
        return jiSuAPIUtils.GetInfoByIPAddress(ip);
    }

    /**
     * 获取当前用户IP地址记录
     *
     * @param sessionId
     * @param type
     * @param start
     * @param size
     * @return
     */
    public List<IPAddressRecord> GetSelfUserAddressRecord(String sessionId, int type
            , int start, int size) {
        User user = userCertificateService.GetUserLoginCertificate(sessionId);
        List<IPAddressRecord> ipAddressRecords = ipAddressMapper.selectIPAddressRecordByType(user.getUsername(), type, start, size);
        if (ipAddressRecords != null) {
            return ipAddressRecords;
        }
        return new ArrayList<>();
    }

    /**
     * 查询其他用户最后的IP地址记录
     *
     * @param username
     * @return
     */
    public IPAddressRecord GetOtherUserLatestPostTypeIPAddress(String username) {
        IPAddressRecord ipAddressRecord = ipAddressMapper.selectLatestIPAddressRecordByType(username, IPAddressEnum.POST.getValue());
        if (ipAddressRecord != null) {
            if (ipAddressRecord.getCountry().equals("中国")) {
                if (ipAddressRecord.getProvince() != null) {
                    //港澳台地区处理
                    if (ipAddressRecord.getProvince().equals("香港")
                            || ipAddressRecord.getProvince().equals("澳门")
                            || ipAddressRecord.getProvince().equals("台湾")) {
                        ipAddressRecord.setArea(ipAddressRecord.getCountry() + ipAddressRecord.getProvince());
                    } else {
                        ipAddressRecord.setArea(ipAddressRecord.getProvince());
                    }
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
    public IPAddressRecord GetSelfUserLatestPostTypeIPAddress(String sessionId) {
        User user = userCertificateService.GetUserLoginCertificate(sessionId);
        IPAddressRecord ipAddressRecord = GetOtherUserLatestPostTypeIPAddress(user.getUsername());
        return ipAddressRecord;
    }

    /**
     * 保存IP地址记录
     *
     * @param ipAddressRecord
     */
    public void SaveIPAddress(IPAddressRecord ipAddressRecord) {
        ipAddressMapper.insertIPAddressRecord(ipAddressRecord);
    }
}
