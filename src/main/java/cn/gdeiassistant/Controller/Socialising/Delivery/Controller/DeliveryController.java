package cn.gdeiassistant.Controller.Socialising.Delivery.Controller;

import cn.gdeiassistant.Exception.DatabaseException.DataNotExistException;
import cn.gdeiassistant.Pojo.Entity.DeliveryOrder;
import cn.gdeiassistant.Pojo.Entity.DeliveryTrade;
import cn.gdeiassistant.Service.AccountManagement.Profile.UserProfileService;
import cn.gdeiassistant.Service.Socialising.Delivery.DeliveryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Controller
public class DeliveryController {

    @Autowired
    private DeliveryService deliveryService;

    @Autowired
    private UserProfileService userProfileService;

    /**
     * 进入全民快递主页
     *
     * @param request
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/delivery", method = RequestMethod.GET)
    public ModelAndView ResolveDeliveryIndexPage(HttpServletRequest request) throws Exception {
        ModelAndView modelAndView = new ModelAndView();
        //获取头像地址
        String url = userProfileService.GetUserAvatar(request.getSession().getId());
        String nickname = userProfileService.GetUserProfile(request.getSession().getId()).getNickname();
        modelAndView.setViewName("Delivery/index");
        modelAndView.addObject("AvatarURL", url);
        modelAndView.addObject("NickName", nickname);
        return modelAndView;
    }

    /**
     * 查看快递订单详细信息
     * <p>
     * DetailType表示信息详细程度
     * 0表示显示所有信息和删除按钮（发布者）
     * 1表示显示大致信息和接单按钮（被接单前的第三方）
     * 2表示没有权限查看（被接单后的第三方）
     * 3表示显示所有信息但是没有操作按钮（接单者）
     *
     * @param request
     * @param id
     * @return
     */
    @RequestMapping(value = "/delivery/order/id/{id}", method = RequestMethod.GET)
    public ModelAndView ResolveDeliveryOrderDetailPage(HttpServletRequest request, @PathVariable("id") Integer id) throws DataNotExistException {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("/Delivery/orderDetail");
        DeliveryOrder deliveryOrder = deliveryService.QueryDeliveryOrderByOrderId(id);
        if (deliveryOrder.getState().equals(1)) {
            DeliveryTrade deliveryTrade = deliveryService.QueryDeliveryTradeByOrderId(deliveryOrder.getOrderId());
            modelAndView.addObject("DeliveryTrade", deliveryTrade);
        }
        modelAndView.addObject("DeliveryOrder", deliveryOrder);
        int detailType = deliveryService.QueryDeliveryOrderDetailType(request.getSession().getId(), id);
        modelAndView.addObject("DetailType", detailType);
        return modelAndView;
    }

    /**
     * 查看代收交易详细信息
     *
     * @param request
     * @param tradeId
     * @return
     */
    @RequestMapping(value = "/delivery/trade/id/{id}", method = RequestMethod.GET)
    public ModelAndView ResolveDeliveryTradeDetailPage(HttpServletRequest request, @PathVariable("id") Integer tradeId) throws DataNotExistException {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("/Delivery/tradeDetail");
        DeliveryTrade deliveryTrade = deliveryService.QueryDeliveryTradeByTradeId(tradeId);
        int detailType = deliveryService.QueryDeliveryTradeDetailType(request.getSession().getId(), tradeId);
        modelAndView.addObject("DeliveryTrade", deliveryTrade);
        modelAndView.addObject("DetailType", detailType);
        return modelAndView;
    }

    /**
     * 进入下单页面
     *
     * @return
     */
    @RequestMapping(value = "/delivery/order", method = RequestMethod.GET)
    public ModelAndView ResolveDeliveryOrderPage(HttpServletRequest request) {
        return new ModelAndView("Delivery/order");
    }

    /**
     * 进入接单界面
     *
     * @return
     */
    @RequestMapping(value = "/delivery/accept", method = RequestMethod.GET)
    public ModelAndView ResolveDeliveryAcceptPage(HttpServletRequest request) {
        return new ModelAndView("Delivery/accept");
    }

    /**
     * 进入个人中心
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/delivery/personal", method = RequestMethod.GET)
    public ModelAndView ResolveDeliveryPersonalPage(HttpServletRequest request) {
        ModelAndView modelAndView = new ModelAndView("Delivery/personal");
        List<DeliveryOrder> personalDeliveryOrderList = deliveryService.QueryPersonalDeliveryOrder(request.getSession().getId());
        List<DeliveryOrder> acceptedDeliveryOrderList = deliveryService.QueryPersonalAcceptedDeliveryOrder(request.getSession().getId());
        modelAndView.addObject("OrderList", personalDeliveryOrderList);
        modelAndView.addObject("AcceptedOrderList", acceptedDeliveryOrderList);
        return modelAndView;
    }

}
