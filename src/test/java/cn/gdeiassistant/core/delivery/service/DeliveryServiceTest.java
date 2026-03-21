package cn.gdeiassistant.core.delivery.service;

import cn.gdeiassistant.common.exception.DeliveryException.NoAccessUpdatingException;
import cn.gdeiassistant.common.exception.DeliveryException.SelfTradingOrderException;
import cn.gdeiassistant.common.pojo.Entity.User;
import cn.gdeiassistant.core.delivery.converter.DeliveryConverter;
import cn.gdeiassistant.core.delivery.mapper.DeliveryMapper;
import cn.gdeiassistant.core.delivery.pojo.entity.DeliveryOrderEntity;
import cn.gdeiassistant.core.delivery.pojo.entity.DeliveryTradeEntity;
import cn.gdeiassistant.core.delivery.pojo.vo.DeliveryOrderVO;
import cn.gdeiassistant.core.delivery.pojo.vo.DeliveryTradeVO;
import cn.gdeiassistant.core.message.service.InteractionNotificationService;
import cn.gdeiassistant.core.userLogin.service.UserCertificateService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeliveryServiceTest {

    @Mock
    private DeliveryMapper deliveryMapper;

    @Mock
    private DeliveryConverter deliveryConverter;

    @Mock
    private UserCertificateService userCertificateService;

    @Mock
    private InteractionNotificationService interactionNotificationService;

    @InjectMocks
    private DeliveryService deliveryService;

    @Test
    void acceptOrderThrowsSelfTradingOrderExceptionWhenAcceptingOwnOrder() {
        when(userCertificateService.getUserLoginCertificate("session1")).thenReturn(new User("owner"));
        when(deliveryMapper.selectDeliveryOrderUsername(1)).thenReturn("owner");

        assertThrows(SelfTradingOrderException.class,
                () -> deliveryService.acceptOrder(1, "session1"));
    }

    @Test
    void queryDeliveryOrderDetailTypeReturns0ForPublisher() throws Exception {
        when(userCertificateService.getUserLoginCertificate("session1")).thenReturn(new User("publisher"));
        DeliveryOrderEntity entity = new DeliveryOrderEntity();
        entity.setOrderId(1);
        entity.setUsername("publisher");
        entity.setState(0);
        when(deliveryMapper.selectDeliveryOrderByOrderId(1)).thenReturn(entity);
        DeliveryOrderVO vo = new DeliveryOrderVO();
        vo.setOrderId(1);
        vo.setUsername("publisher");
        vo.setState(0);
        when(deliveryConverter.toOrderVO(entity)).thenReturn(vo);

        int type = deliveryService.queryDeliveryOrderDetailType("session1", 1);

        assertEquals(0, type);
    }

    @Test
    void queryDeliveryOrderDetailTypeReturns3ForRunner() throws Exception {
        when(userCertificateService.getUserLoginCertificate("session1")).thenReturn(new User("runner"));
        DeliveryOrderEntity entity = new DeliveryOrderEntity();
        entity.setOrderId(1);
        entity.setUsername("publisher");
        entity.setState(1);
        when(deliveryMapper.selectDeliveryOrderByOrderId(1)).thenReturn(entity);
        DeliveryOrderVO vo = new DeliveryOrderVO();
        vo.setOrderId(1);
        vo.setUsername("publisher");
        vo.setState(1);
        when(deliveryConverter.toOrderVO(entity)).thenReturn(vo);
        when(deliveryMapper.selectDeliveryTradeUsernameByOrderId(1)).thenReturn("runner");

        int type = deliveryService.queryDeliveryOrderDetailType("session1", 1);

        assertEquals(3, type);
    }

    @Test
    void queryDeliveryOrderDetailTypeReturns1ForVisitor() throws Exception {
        when(userCertificateService.getUserLoginCertificate("session1")).thenReturn(new User("visitor"));
        DeliveryOrderEntity entity = new DeliveryOrderEntity();
        entity.setOrderId(1);
        entity.setUsername("publisher");
        entity.setState(0);
        when(deliveryMapper.selectDeliveryOrderByOrderId(1)).thenReturn(entity);
        DeliveryOrderVO vo = new DeliveryOrderVO();
        vo.setOrderId(1);
        vo.setUsername("publisher");
        vo.setState(0);
        when(deliveryConverter.toOrderVO(entity)).thenReturn(vo);

        int type = deliveryService.queryDeliveryOrderDetailType("session1", 1);

        assertEquals(1, type);
    }

    @Test
    void deleteOrderThrowsNoAccessUpdatingExceptionWhenNonOwnerTriesToDelete() {
        when(userCertificateService.getUserLoginCertificate("session1")).thenReturn(new User("other"));
        DeliveryOrderEntity entity = new DeliveryOrderEntity();
        entity.setOrderId(1);
        entity.setUsername("owner");
        entity.setState(0);
        when(deliveryMapper.selectDeliveryOrderByOrderId(1)).thenReturn(entity);

        assertThrows(NoAccessUpdatingException.class,
                () -> deliveryService.deleteOrder(1, "session1"));
    }

    @Test
    void finishTradeSucceedsForOrderPublisher() throws Exception {
        when(userCertificateService.getUserLoginCertificate("session1")).thenReturn(new User("publisher"));
        DeliveryTradeEntity trade = new DeliveryTradeEntity();
        trade.setTradeId(10);
        trade.setOrderId(1);
        trade.setUsername("runner");
        when(deliveryMapper.selectDeliveryTradeByTradeId(10)).thenReturn(trade);
        DeliveryOrderEntity order = new DeliveryOrderEntity();
        order.setOrderId(1);
        order.setUsername("publisher");
        order.setCompany("SF Express");
        when(deliveryMapper.selectDeliveryOrderByOrderId(1)).thenReturn(order);
        when(deliveryMapper.finishOrder(1)).thenReturn(1);
        when(deliveryMapper.updateTradeState(10, 0, 1)).thenReturn(1);

        deliveryService.finishTrade(10, "session1");

        verify(deliveryMapper).finishOrder(1);
        verify(deliveryMapper).updateTradeState(10, 0, 1);
        verify(interactionNotificationService).createInteractionNotification(
                eq("delivery"), eq("order_finished"), eq("runner"), eq("publisher"),
                eq("1"), eq("10"), eq("accepted"), anyString(), anyString()
        );
    }
}
