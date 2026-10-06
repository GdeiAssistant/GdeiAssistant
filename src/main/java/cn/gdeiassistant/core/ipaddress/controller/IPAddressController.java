package cn.gdeiassistant.core.ipaddress.controller;

import cn.gdeiassistant.common.enums.ipaddress.IPAddressEnum;
import cn.gdeiassistant.common.pojo.entity.IPAddressRecord;
import cn.gdeiassistant.common.pojo.result.DataJsonResult;
import cn.gdeiassistant.common.tools.utils.PageUtils;
import cn.gdeiassistant.core.ipaddress.service.IPAddressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
public class IPAddressController {

    @Autowired
    private IPAddressService ipAddressService;

    /**
     * 获取当前用户最近登录记录
     *
     * @param request
     * @param start
     * @param size
     * @return
     */
    @RequestMapping(value = "/api/ip/start/{start}/size/{size}", method = RequestMethod.GET)
    public DataJsonResult<List<IPAddressRecord>> getRecentLoginIPAddressRecord(HttpServletRequest request
            , @PathVariable("start") int start, @PathVariable("size") int size) {
        size = PageUtils.normalizePageSize(start, size);
        String sessionId = (String) request.getAttribute("sessionId");
        List<IPAddressRecord> recordList = ipAddressService.getSelfUserAddressRecord(sessionId
                , IPAddressEnum.LOGIN.getValue(), start, size);
        return new DataJsonResult<>(true, recordList);
    }

    /**
     * 获取IP属地
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/api/ip/area", method = RequestMethod.GET)
    public DataJsonResult<String> getIPAddressArea(HttpServletRequest request) {
        String sessionId = (String) request.getAttribute("sessionId");
        IPAddressRecord ipAddressRecord = ipAddressService.getSelfUserLatestPostTypeIPAddress(sessionId);
        return new DataJsonResult<>(true, ipAddressRecord.getArea());
    }
}
