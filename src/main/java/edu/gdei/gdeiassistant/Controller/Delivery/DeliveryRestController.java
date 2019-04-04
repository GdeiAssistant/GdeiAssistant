package edu.gdei.gdeiassistant.Controller.Delivery;

import com.taobao.wsgsvr.WsgException;
import edu.gdei.gdeiassistant.Exception.DatabaseException.DataNotExistException;
import edu.gdei.gdeiassistant.Exception.DeliveryException.DeliveryOrderStateUpdatedException;
import edu.gdei.gdeiassistant.Exception.DeliveryException.NoAccessUpdatingException;
import edu.gdei.gdeiassistant.Pojo.Entity.DeliveryOrder;
import edu.gdei.gdeiassistant.Pojo.Result.DataJsonResult;
import edu.gdei.gdeiassistant.Pojo.Result.JsonResult;
import edu.gdei.gdeiassistant.Service.Delivery.DeliveryService;
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
    public DataJsonResult<List<DeliveryOrder>> QueryDeliveryOrderPage(@PathVariable("start") Integer start
            , @PathVariable("size") Integer size) throws WsgException {
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
        String username = (String) request.getSession().getAttribute("username");
        deliveryService.AcceptOrder(orderId, username);
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
     * @throws WsgException
     */
    @RequestMapping(value = "/api/delivery/order/id/{id}", method = RequestMethod.DELETE)
    public JsonResult DeleteOrder(HttpServletRequest request, @PathVariable("id") Integer orderId) throws NoAccessUpdatingException, DataNotExistException, WsgException, DeliveryOrderStateUpdatedException {
        String username = (String) request.getSession().getAttribute("username");
        deliveryService.DeleteOrder(orderId, username);
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
     * @throws WsgException
     */
    @RequestMapping(value = "/api/delivery/trade/id/{id}/finishtrade", method = RequestMethod.POST)
    public JsonResult FinishTrade(HttpServletRequest request, @PathVariable("id") Integer tradeId) throws DataNotExistException, NoAccessUpdatingException, WsgException {
        String username = (String) request.getSession().getAttribute("username");
        deliveryService.FinishTrade(tradeId, username);
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
    public JsonResult AddDeliveryOrder(HttpServletRequest request, DeliveryOrder deliveryOrder) throws WsgException {
        String username = (String) request.getSession().getAttribute("username");
        deliveryOrder.setUsername(username);
        deliveryService.AddDeliveryOrder(deliveryOrder);
        return new JsonResult(true);
    }
}
