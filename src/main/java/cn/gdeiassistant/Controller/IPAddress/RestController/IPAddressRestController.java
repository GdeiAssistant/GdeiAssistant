package cn.gdeiassistant.Controller.IPAddress.RestController;

import cn.gdeiassistant.Enum.IPAddress.IPAddressEnum;
import cn.gdeiassistant.Pojo.Entity.IPAddressRecord;
import cn.gdeiassistant.Pojo.Result.DataJsonResult;
import cn.gdeiassistant.Service.IPAddress.IPAddressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
public class IPAddressRestController {

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
    public DataJsonResult<List<IPAddressRecord>> GetRecentLoginIPAddressRecord(HttpServletRequest request
            , @PathVariable("start") int start, @PathVariable("size") int size) {
        List<IPAddressRecord> recordList = ipAddressService.GetSelfUserAddressRecord(request.getSession().getId()
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
    public DataJsonResult<String> GetIPAddressArea(HttpServletRequest request) {
        IPAddressRecord ipAddressRecord = ipAddressService.GetSelfUserLatestPostTypeIPAddress(request.getSession().getId());
        return new DataJsonResult<>(true, ipAddressRecord.getArea());
    }
}
