package cn.gdeiassistant.core.delivery.controller;

import cn.gdeiassistant.common.annotation.RateLimit;
import cn.gdeiassistant.common.annotation.RecordIPAddress;
import cn.gdeiassistant.common.enums.IPAddress.IPAddressEnum;
import cn.gdeiassistant.common.exception.DatabaseException.DataNotExistException;
import cn.gdeiassistant.common.exception.DeliveryException.DeliveryOrderStateUpdatedException;
import cn.gdeiassistant.common.exception.DeliveryException.NoAccessUpdatingException;
import cn.gdeiassistant.common.pojo.Result.DataJsonResult;
import cn.gdeiassistant.common.pojo.Result.JsonResult;
import cn.gdeiassistant.common.tools.Utils.PageUtils;
import cn.gdeiassistant.core.delivery.pojo.dto.DeliveryPublishDTO;
import cn.gdeiassistant.core.delivery.pojo.vo.DeliveryOrderVO;
import cn.gdeiassistant.core.delivery.pojo.vo.DeliveryTradeVO;
import cn.gdeiassistant.core.delivery.service.DeliveryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.Min;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@Validated
public class DeliveryController {

    @Autowired
    private DeliveryService deliveryService;

    /**
     * 订单详情（含 detailType；已接单时含 trade）。GET /api/delivery/order/id/{id}
     */
    @RequestMapping(value = "/api/delivery/order/id/{id}", method = RequestMethod.GET)
    public DataJsonResult<Map<String, Object>> getDeliveryOrderDetail(HttpServletRequest request, @PathVariable("id") Integer id) throws DataNotExistException {
        String sessionId = (String) request.getAttribute("sessionId");
        DeliveryOrderVO order = deliveryService.queryDeliveryOrderByOrderId(id);
        int detailType = deliveryService.queryDeliveryOrderDetailType(sessionId, id);
        Map<String, Object> data = new HashMap<>();
        data.put("detailType", detailType);
        if (detailType == 1 || detailType == 2) {
            // Unauthorized viewers: strip sensitive fields and omit trade info
            order.setNumber(null);
            order.setPhone(null);
            data.put("order", order);
        } else {
            // Owner (0) or runner (3): return full data including trade
            data.put("order", order);
            if (order.getState() != null && !order.getState().equals(0)) {
                DeliveryTradeVO trade = deliveryService.queryDeliveryTradeByOrderId(order.getOrderId());
                data.put("trade", trade);
            }
        }
        return new DataJsonResult<>(true, data);
    }

    /**
     * 我的跑腿：我发布的 + 我接的单。GET /api/delivery/mine
     */
    @RequestMapping(value = "/api/delivery/mine", method = RequestMethod.GET)
    public DataJsonResult<Map<String, Object>> getMyDelivery(HttpServletRequest request) {
        String sessionId = (String) request.getAttribute("sessionId");
        List<DeliveryOrderVO> published = deliveryService.queryPersonalDeliveryOrder(sessionId);
        List<DeliveryOrderVO> accepted = deliveryService.queryPersonalAcceptedDeliveryOrder(sessionId);
        Map<String, Object> data = new HashMap<>();
        data.put("published", published);
        data.put("accepted", accepted);
        return new DataJsonResult<>(true, data);
    }

    /**
     * 分页查询快递代收订单
     *
     * @param start
     * @param size
     * @return
     */
    @RequestMapping(value = "/api/delivery/order/start/{start}/size/{size}", method = RequestMethod.GET)
    public DataJsonResult<List<DeliveryOrderVO>> queryDeliveryOrderPage(HttpServletRequest request, @PathVariable("start") @Min(0) Integer start
            , @PathVariable("size") @Min(1) Integer size) {
        size = PageUtils.normalizePageSize(start, size);
        List<DeliveryOrderVO> list = deliveryService.queryDeliveryOrderPage(start, size);
        return new DataJsonResult<>(true, list);
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
    public JsonResult acceptOrder(HttpServletRequest request, @RequestParam Integer orderId) throws Exception {
        String sessionId = (String) request.getAttribute("sessionId");
        deliveryService.acceptOrder(orderId, sessionId);
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
    public JsonResult deleteOrder(HttpServletRequest request, @PathVariable("id") Integer orderId) throws NoAccessUpdatingException, DataNotExistException, DeliveryOrderStateUpdatedException {
        String sessionId = (String) request.getAttribute("sessionId");
        deliveryService.deleteOrder(orderId, sessionId);
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
    public JsonResult finishTrade(HttpServletRequest request, @PathVariable("id") Integer tradeId) throws DataNotExistException, NoAccessUpdatingException {
        String sessionId = (String) request.getAttribute("sessionId");
        deliveryService.finishTrade(tradeId, sessionId);
        return new JsonResult(true);
    }

    /**
     * 添加快递代收订单
     *
     * @param request
     * @param deliveryOrder
     * @return
     */
    @RateLimit(maxRequests = 5, windowSeconds = 60)
    @RequestMapping(value = "/api/delivery/order", method = RequestMethod.POST)
    @RecordIPAddress(type = IPAddressEnum.POST)
    public JsonResult addDeliveryOrder(HttpServletRequest request, @Validated DeliveryPublishDTO dto) {
        String sessionId = (String) request.getAttribute("sessionId");
        deliveryService.addDeliveryOrder(sessionId, dto);
        return new JsonResult(true);
    }
}
