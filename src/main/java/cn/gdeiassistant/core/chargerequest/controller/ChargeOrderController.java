package cn.gdeiassistant.core.chargerequest.controller;

import cn.gdeiassistant.common.annotation.RestAuthentication;
import cn.gdeiassistant.common.pojo.Entity.User;
import cn.gdeiassistant.common.pojo.Result.DataJsonResult;
import cn.gdeiassistant.core.charge.pojo.vo.ChargeOrderVO;
import cn.gdeiassistant.core.charge.service.ChargeOrderService;
import cn.gdeiassistant.core.userLogin.service.UserCertificateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
public class ChargeOrderController {

    @Autowired
    private ChargeOrderService chargeOrderService;

    @Autowired
    private UserCertificateService userCertificateService;

    @RequestMapping(value = "/api/card/charge/orders/{orderId}", method = RequestMethod.GET)
    @RestAuthentication
    public DataJsonResult<ChargeOrderVO> queryChargeOrder(HttpServletRequest request,
                                                          @PathVariable("orderId") String orderId) throws Exception {
        String username = currentUsername(request);
        return new DataJsonResult<>(true, chargeOrderService.queryOrder(orderId, username));
    }

    @RequestMapping(value = "/api/card/charge/orders", method = RequestMethod.GET)
    @RestAuthentication
    public DataJsonResult<List<ChargeOrderVO>> queryChargeOrderList(HttpServletRequest request,
                                                                    @RequestParam(value = "page", required = false)
                                                                    Integer page,
                                                                    @RequestParam(value = "size", required = false)
                                                                    Integer size,
                                                                    @RequestParam(value = "status", required = false)
                                                                    String status) {
        String username = currentUsername(request);
        return new DataJsonResult<>(true, chargeOrderService.queryRecentOrders(username, page, size, status));
    }

    private String currentUsername(HttpServletRequest request) {
        String sessionId = (String) request.getAttribute("sessionId");
        User user = userCertificateService.getUserLoginCertificate(sessionId);
        return user.getUsername();
    }
}
