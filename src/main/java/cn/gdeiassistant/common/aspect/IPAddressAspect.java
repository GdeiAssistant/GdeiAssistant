package cn.gdeiassistant.common.aspect;

import cn.gdeiassistant.common.annotation.RecordIPAddress;
import cn.gdeiassistant.common.pojo.entity.IPAddressRecord;
import cn.gdeiassistant.common.pojo.entity.User;
import cn.gdeiassistant.core.ipaddress.service.IPAddressService;
import cn.gdeiassistant.core.userlogin.service.UserCertificateService;
import cn.gdeiassistant.common.tools.utils.IPAddressUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;

@Aspect
@Component
@Order(1)
public class IPAddressAspect {

    @Autowired
    private IPAddressService ipAddressService;

    @Autowired
    private UserCertificateService userCertificateService;

    @After("@annotation(annotation)")
    public void saveLoginIPAddress(JoinPoint joinPoint, RecordIPAddress annotation) {
        Object[] args = joinPoint.getArgs();
        HttpServletRequest request = (HttpServletRequest) args[0];
        // 统一从 JwtSessionIdFilter 注入的 attribute 获取 sessionId
        String sessionId = (String) request.getAttribute("sessionId");
        User user = userCertificateService.getUserLoginCertificate(sessionId);
        if (user != null) {
            //获取IP地址信息
            String ip = IPAddressUtils.getRequestRealIpAddress(request);
            //获取IP归属地和网络类型
            IPAddressRecord record = ipAddressService.getInfoByIPAddress(ip);
            //保存IP记录
            if (record != null) {
                record.setIp(ip);
                record.setUsername(user.getUsername());
                record.setType(annotation.type().getValue());
                ipAddressService.saveIPAddress(record);
            }
        }
        //匿名用户不记录IP地址
    }
}

