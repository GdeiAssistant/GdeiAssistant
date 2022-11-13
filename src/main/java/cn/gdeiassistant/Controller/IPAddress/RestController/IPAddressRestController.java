package cn.gdeiassistant.Controller.IPAddress.RestController;

import cn.gdeiassistant.Pojo.Entity.IPAddressRecord;
import cn.gdeiassistant.Pojo.Result.DataJsonResult;
import cn.gdeiassistant.Service.IPAddress.IPAddressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
public class IPAddressRestController {

    @Autowired
    private IPAddressService ipAddressService;

    /**
     * 获取IP属地
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/api/ip/area", method = RequestMethod.GET)
    public DataJsonResult<String> GetIPAddressArea(HttpServletRequest request) {
        IPAddressRecord ipAddressRecord = ipAddressService.QuerySelfUserLatestPostTypeIPAddress(request.getSession().getId());
        return new DataJsonResult<>(true, ipAddressRecord.getArea());
    }
}
