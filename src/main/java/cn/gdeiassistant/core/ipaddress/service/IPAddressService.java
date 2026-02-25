package cn.gdeiassistant.core.iPAddress.service;

import cn.gdeiassistant.common.enums.IPAddress.IPAddressEnum;
import cn.gdeiassistant.common.pojo.Entity.IPAddressRecord;
import cn.gdeiassistant.common.pojo.Entity.User;
import cn.gdeiassistant.core.iPAddress.mapper.IPAddressMapper;
import cn.gdeiassistant.core.userLogin.service.UserCertificateService;
import cn.gdeiassistant.common.tools.SpringUtils.JiSuAPIUtils;
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
    public IPAddressRecord getInfoByIPAddress(String ip) {
        return jiSuAPIUtils.getInfoByIPAddress(ip);
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
    public List<IPAddressRecord> getSelfUserAddressRecord(String sessionId, int type
            , int start, int size) {
        User user = userCertificateService.getUserLoginCertificate(sessionId);
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
    public IPAddressRecord getOtherUserLatestPostTypeIPAddress(String username) {
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
    public IPAddressRecord getSelfUserLatestPostTypeIPAddress(String sessionId) {
        User user = userCertificateService.getUserLoginCertificate(sessionId);
        IPAddressRecord ipAddressRecord = getOtherUserLatestPostTypeIPAddress(user.getUsername());
        return ipAddressRecord;
    }

    /**
     * 保存IP地址记录
     *
     * @param ipAddressRecord
     */
    public void saveIPAddress(IPAddressRecord ipAddressRecord) {
        ipAddressMapper.insertIPAddressRecord(ipAddressRecord);
    }
}
