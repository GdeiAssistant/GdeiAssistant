package cn.gdeiassistant.Aspect;

import cn.gdeiassistant.Annotation.RecordIPAddress;
import cn.gdeiassistant.Pojo.Entity.IPAddressRecord;
import cn.gdeiassistant.Pojo.Entity.Location;
import cn.gdeiassistant.Service.AccountManagement.Token.LoginTokenService;
import cn.gdeiassistant.Service.OpenAPI.IPAddress.IPAddressService;
import cn.gdeiassistant.Tools.Utils.IPAddressUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

@Aspect
@Component
@Order(1)
public class IPAddressAspect {

    @Autowired
    private IPAddressService ipAddressService;

    @Autowired
    private LoginTokenService loginTokenService;

    @After("@annotation(annotation)")
    public void SaveLoginIPAddress(JoinPoint joinPoint, RecordIPAddress annotation) {
        Object[] args = joinPoint.getArgs();
        HttpServletRequest request = (HttpServletRequest) args[0];
        //获取IP地址信息
        String ip = IPAddressUtils.GetRequestRealIPAddress(request);
        //获取IP归属地
        Location location = ipAddressService.GetLocationInfoByIPAddress(ip);
        //获取用户名
        String sessionId = null;
        if (annotation.rest()) {
            //令牌资源访问
            String token = request.getParameter("token");
            sessionId = loginTokenService.ParseToken(token).get("sessionId").asString();
        }
        //API访问
        sessionId = request.getSession().getId();
        //保存IP记录
        IPAddressRecord record = new IPAddressRecord();
        record.setIp(ip);
        record.setType(annotation.type().getValue());
        record.setCountry(location.getCountry());
        record.setProvince(location.getProvince());
        record.setCity(location.getCity());
        ipAddressService.SaveIPAddress(sessionId, record);
    }
}
