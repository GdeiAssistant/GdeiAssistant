package edu.gdei.gdeiassistant.Controller.Delivery;

import com.taobao.wsgsvr.WsgException;
import edu.gdei.gdeiassistant.Exception.DatabaseException.DataNotExistException;
import edu.gdei.gdeiassistant.Pojo.Entity.DeliveryOrder;
import edu.gdei.gdeiassistant.Pojo.Entity.DeliveryTrade;
import edu.gdei.gdeiassistant.Service.Delivery.DeliveryService;
import edu.gdei.gdeiassistant.Service.Profile.UserProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
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
        String username = (String) request.getSession().getAttribute("username");
        //获取头像地址
        String url = userProfileService.GetUserAvatar(username);
        String kickname = userProfileService.GetUserProfile(username).getKickname();
        modelAndView.setViewName("Delivery/index");
        modelAndView.addObject("AvatarURL", url);
        modelAndView.addObject("KickName", kickname);
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
    public ModelAndView ResolveDeliveryOrderDetailPage(HttpServletRequest request, @PathVariable("id") Integer id) throws DataNotExistException, WsgException {
        String username = (String) request.getSession().getAttribute("username");
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("/Delivery/orderDetail");
        DeliveryOrder deliveryOrder = deliveryService.QueryDeliveryOrderByOrderId(id);
        if (deliveryOrder.getState().equals(1)) {
            DeliveryTrade deliveryTrade = deliveryService.QueryDeliveryTradeByOrderId(deliveryOrder.getOrderId());
            modelAndView.addObject("DeliveryTrade", deliveryTrade);
        }
        modelAndView.addObject("DeliveryOrder", deliveryOrder);
        if (deliveryOrder.getUsername().equals(username)) {
            //自己发布的订单信息
            modelAndView.addObject("DetailType", 0);
        } else {
            //他人发布的订单信息
            if (deliveryOrder.getState().equals(0)) {
                //等待接单，可以查看
                modelAndView.addObject("DetailType", 1);
            } else {
                //已经接单，检查当前用户是否为接单人
                if (deliveryService.CheckOrderAccepter(deliveryOrder.getOrderId(), username)) {
                    //当前用户为接单人，可以查看详细信息
                    modelAndView.addObject("DetailType", 3);
                } else {
                    //没有权限查看
                    modelAndView.addObject("DetailType", 2);
                }
            }
        }
        return modelAndView;
    }

    /**
     * 查看代收交易详细信息
     * <p>
     * DetailType表示信息详细程度
     * 0表示显示所有信息且显示确认交付按钮（下单者）
     * 1表示没有权限查看（第三方）
     * 2表示显示所有信息但没有操作按钮（接单者）
     *
     * @param request
     * @param tradeId
     * @return
     */
    @RequestMapping(value = "/delivery/trade/id/{id}", method = RequestMethod.GET)
    public ModelAndView ResolveDeliveryTradeDetailPage(HttpServletRequest request, @PathVariable("id") Integer tradeId) throws WsgException, DataNotExistException {
        String username = (String) request.getSession().getAttribute("username");
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("/Delivery/tradeDetail");
        DeliveryTrade deliveryTrade = deliveryService.QueryDeliveryTradeByTradeId(tradeId);
        modelAndView.addObject("DeliveryTrade", deliveryTrade);
        if (deliveryTrade.getUsername().equals(username)) {
            //自己接受的交易单
            modelAndView.addObject("DetailType", 2);
        } else {
            //他人接受的交易单，检查当前用户是否为下单人
            if (deliveryService.CheckOrderPublisher(deliveryTrade.getOrderId(), username)) {
                //当前用户为下单人，可以查看详细信息和交付按钮
                modelAndView.addObject("DetailType", 0);
            } else {
                //没有权限查看
                modelAndView.addObject("DetailType", 1);
            }
        }
        return modelAndView;
    }

    /**
     * 进入下单页面
     *
     * @return
     */
    @RequestMapping(value = "/delivery/order", method = RequestMethod.GET)
    public ModelAndView ResolveDeliveryOrderPage() {
        return new ModelAndView("Delivery/order");
    }

    /**
     * 进入接单界面
     *
     * @return
     */
    @RequestMapping(value = "/delivery/accept", method = RequestMethod.GET)
    public ModelAndView ResolveDeliveryAcceptPage() {
        return new ModelAndView("Delivery/accept");
    }

    /**
     * 进入个人中心
     *
     * @param request
     * @return
     * @throws WsgException
     */
    @RequestMapping(value = "/delivery/personal", method = RequestMethod.GET)
    public ModelAndView ResolveDeliveryPersonalPage(HttpServletRequest request) throws WsgException {
        String username = (String) request.getSession().getAttribute("username");
        ModelAndView modelAndView = new ModelAndView("Delivery/personal");
        List<DeliveryOrder> list = deliveryService.QueryPersonalDeliveryOrder(username);
        List<DeliveryOrder> deliveryOrderList = new ArrayList<>();
        List<DeliveryOrder> acceptedDeliveryOrderList = new ArrayList<>();
        for (DeliveryOrder deliveryOrder : list) {
            if (deliveryOrder.getUsername().equals(username)) {
                deliveryOrderList.add(deliveryOrder);
            } else {
                acceptedDeliveryOrderList.add(deliveryOrder);
            }
        }
        modelAndView.addObject("OrderList", deliveryOrderList);
        modelAndView.addObject("AcceptedOrderList", acceptedDeliveryOrderList);
        return modelAndView;
    }

}
