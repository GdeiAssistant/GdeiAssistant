package cn.gdeiassistant.core.delivery.controller;

import cn.gdeiassistant.common.annotation.RateLimit;
import cn.gdeiassistant.common.annotation.RecordIPAddress;
import cn.gdeiassistant.common.enums.ipaddress.IPAddressEnum;
import cn.gdeiassistant.common.exception.databaseexception.DataNotExistException;
import cn.gdeiassistant.common.exception.deliveryexception.DeliveryOrderStateUpdatedException;
import cn.gdeiassistant.common.exception.deliveryexception.NoAccessUpdatingException;
import cn.gdeiassistant.common.pojo.result.DataJsonResult;
import cn.gdeiassistant.common.pojo.result.JsonResult;
import cn.gdeiassistant.common.tools.utils.PageUtils;
import cn.gdeiassistant.core.delivery.pojo.dto.DeliveryPublishDTO;
import cn.gdeiassistant.core.delivery.pojo.vo.DeliveryOrderVO;
import cn.gdeiassistant.core.delivery.pojo.vo.DeliveryTradeVO;
import cn.gdeiassistant.core.delivery.service.DeliveryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.Min;
import java.util.List;

@RestController
@Validated
public class DeliveryController {

    @Autowired
    private DeliveryService deliveryService;

    private int requirePositiveId(Integer id) {
        if (id == null || id < 1) {
            throw new IllegalArgumentException("请求参数不合法");
        }
        return id;
    }

    /**
     * 订单详情（含 detailType；已接单时含 trade）。GET /api/delivery/order/id/{id}
     */
    @RequestMapping(value = "/api/delivery/order/id/{id}", method = RequestMethod.GET)
    public DataJsonResult<DeliveryDetailResponse> getDeliveryOrderDetail(HttpServletRequest request, @PathVariable("id") @Min(1) Integer id) throws DataNotExistException {
        id = requirePositiveId(id);
        String sessionId = (String) request.getAttribute("sessionId");
        int detailType = deliveryService.queryDeliveryOrderDetailType(sessionId, id);
        if (detailType == 2) {
            throw new DataNotExistException("该订单不可访问");
        }
        DeliveryOrderVO order = deliveryService.queryDeliveryOrderByOrderId(id);
        DeliveryTradeVO trade = null;
        if (detailType == 1) {
            DeliveryService.redactPublicOrder(order);
        } else if (Integer.valueOf(1).equals(order.getState()) || Integer.valueOf(2).equals(order.getState())) {
            trade = deliveryService.queryDeliveryTradeByOrderId(id);
        }
        return new DataJsonResult<>(true, new DeliveryDetailResponse(order, detailType, trade));
    }

    public record DeliveryDetailResponse(DeliveryOrderVO order, int detailType, DeliveryTradeVO trade) {}
    public record DeliveryMineResponse(List<DeliveryOrderVO> published, List<DeliveryOrderVO> accepted) {}

    /**
     * 我的跑腿：我发布的 + 我接的单。GET /api/delivery/mine
     */
    @RequestMapping(value = "/api/delivery/mine", method = RequestMethod.GET)
    public DataJsonResult<DeliveryMineResponse> getMyDelivery(HttpServletRequest request) {
        String sessionId = (String) request.getAttribute("sessionId");
        List<DeliveryOrderVO> published = deliveryService.queryPersonalDeliveryOrder(sessionId);
        List<DeliveryOrderVO> accepted = deliveryService.queryPersonalAcceptedDeliveryOrder(sessionId);
        return new DataJsonResult<>(true, new DeliveryMineResponse(published, accepted));
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
    public JsonResult acceptOrder(HttpServletRequest request, @RequestParam @Min(1) Integer orderId) throws Exception {
        orderId = requirePositiveId(orderId);
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
    public JsonResult deleteOrder(HttpServletRequest request, @PathVariable("id") @Min(1) Integer orderId) throws NoAccessUpdatingException, DataNotExistException, DeliveryOrderStateUpdatedException {
        orderId = requirePositiveId(orderId);
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
    public JsonResult finishTrade(HttpServletRequest request, @PathVariable("id") @Min(1) Integer tradeId) throws DataNotExistException, NoAccessUpdatingException {
        tradeId = requirePositiveId(tradeId);
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
    public JsonResult addDeliveryOrder(HttpServletRequest request, @RequestBody @Validated DeliveryPublishDTO dto) {
        String sessionId = (String) request.getAttribute("sessionId");
        deliveryService.addDeliveryOrder(sessionId, dto);
        return new JsonResult(true);
    }
}
