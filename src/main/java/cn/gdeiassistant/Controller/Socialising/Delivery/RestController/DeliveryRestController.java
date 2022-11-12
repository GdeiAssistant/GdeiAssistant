package cn.gdeiassistant.Controller.Socialising.Delivery.RestController;

import cn.gdeiassistant.Annotation.RecordIPAddress;
import cn.gdeiassistant.Enum.IPAddress.IPAddressEnum;
import cn.gdeiassistant.Exception.DatabaseException.DataNotExistException;
import cn.gdeiassistant.Exception.DeliveryException.DeliveryOrderStateUpdatedException;
import cn.gdeiassistant.Exception.DeliveryException.NoAccessUpdatingException;
import cn.gdeiassistant.Pojo.Entity.DeliveryOrder;
import cn.gdeiassistant.Pojo.Result.DataJsonResult;
import cn.gdeiassistant.Pojo.Result.JsonResult;
import cn.gdeiassistant.Service.Socialising.Delivery.DeliveryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
public class DeliveryRestController {

    @Autowired
    private DeliveryService deliveryService;

    /**
     * 分页查询快递代收订单
     *
     * @param start
     * @param size
     * @return
     */
    @RequestMapping(value = "/api/delivery/order/start/{start}/size/{size}", method = RequestMethod.GET)
    public DataJsonResult<List<DeliveryOrder>> QueryDeliveryOrderPage(HttpServletRequest request, @PathVariable("start") Integer start
            , @PathVariable("size") Integer size) {
        List<DeliveryOrder> deliveryOrderList = deliveryService.QueryDeliveryOrderPage(start, size);
        return new DataJsonResult<>(true, deliveryOrderList);
    }

    /**
     * 用户接单
     *
     * @param request
     * @param orderId
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/api/delivery/acceptorder", method = RequestMethod.POST)
    public JsonResult AcceptOrder(HttpServletRequest request, Integer orderId) throws Exception {
        deliveryService.AcceptOrder(orderId, request.getSession().getId());
        return new JsonResult(true);
    }

    /**
     * 删除订单
     *
     * @param request
     * @param orderId
     * @return
     * @throws NoAccessUpdatingException
     * @throws DataNotExistException
     * @throws DeliveryOrderStateUpdatedException
     */
    @RequestMapping(value = "/api/delivery/order/id/{id}", method = RequestMethod.DELETE)
    public JsonResult DeleteOrder(HttpServletRequest request, @PathVariable("id") Integer orderId) throws NoAccessUpdatingException, DataNotExistException, DeliveryOrderStateUpdatedException {
        deliveryService.DeleteOrder(orderId, request.getSession().getId());
        return new JsonResult(true);
    }

    /**
     * 更新订单状态，确认已交付快递
     *
     * @param request
     * @param tradeId
     * @return
     * @throws DataNotExistException
     * @throws NoAccessUpdatingException
     */
    @RequestMapping(value = "/api/delivery/trade/id/{id}/finishtrade", method = RequestMethod.POST)
    public JsonResult FinishTrade(HttpServletRequest request, @PathVariable("id") Integer tradeId) throws DataNotExistException, NoAccessUpdatingException {
        deliveryService.FinishTrade(tradeId, request.getSession().getId());
        return new JsonResult(true);
    }

    /**
     * 添加快递代收订单
     *
     * @param request
     * @param deliveryOrder
     * @return
     */
    @RequestMapping(value = "/api/delivery/order", method = RequestMethod.POST)
    @RecordIPAddress(type = IPAddressEnum.POST)
    public JsonResult AddDeliveryOrder(HttpServletRequest request, DeliveryOrder deliveryOrder) {
        deliveryService.AddDeliveryOrder(request.getSession().getId(), deliveryOrder);
        return new JsonResult(true);
    }
}
