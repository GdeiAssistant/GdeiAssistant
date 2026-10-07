package cn.gdeiassistant.core.delivery.service;

import cn.gdeiassistant.common.exception.databaseexception.DataNotExistException;
import cn.gdeiassistant.common.exception.deliveryexception.DeliveryOrderStateUpdatedException;
import cn.gdeiassistant.common.pojo.entity.User;
import cn.gdeiassistant.core.delivery.converter.DeliveryConverter;
import cn.gdeiassistant.core.delivery.mapper.DeliveryMapper;
import cn.gdeiassistant.core.delivery.pojo.dto.DeliveryPublishDTO;
import cn.gdeiassistant.core.delivery.pojo.entity.DeliveryOrderEntity;
import cn.gdeiassistant.core.delivery.pojo.entity.DeliveryTradeEntity;
import cn.gdeiassistant.core.user.service.PublicAuthorResolver;
import cn.gdeiassistant.core.userlogin.service.UserCertificateService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mapstruct.factory.Mappers;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DeliveryPrivacyFlowTest {
    private final DeliveryMapper mapper = mock(DeliveryMapper.class);
    private final UserCertificateService certificates = mock(UserCertificateService.class);
    private final PublicAuthorResolver authors = mock(PublicAuthorResolver.class);
    private final DeliveryService service = new DeliveryService();

    @BeforeEach void setup() {
        ReflectionTestUtils.setField(service, "deliveryMapper", mapper);
        ReflectionTestUtils.setField(service, "deliveryConverter", Mappers.getMapper(DeliveryConverter.class));
        ReflectionTestUtils.setField(service, "userCertificateService", certificates);
        ReflectionTestUtils.setField(service, "publicAuthorResolver", authors);
        when(certificates.getUserLoginCertificate("session")).thenReturn(new User("owner"));
        when(authors.resolve(anyString())).thenReturn(new PublicAuthorResolver.AuthorPublic("public-id", "公开昵称"));
    }

    private DeliveryOrderEntity order() {
        var order = new DeliveryOrderEntity(); order.setOrderId(7); order.setUsername("owner");
        order.setTaskName("合成任务"); order.setPickupCode("synthetic-code"); order.setContactPhone("synthetic-phone");
        order.setPrice(new BigDecimal("2.50")); order.setPickupLocation("合成驿站");
        order.setDeliveryAddress("合成地址"); order.setRemarks("合成备注"); order.setState(0);
        return order;
    }

    @Test void publicListRedactsPrivateFieldsWithoutChangingTheStoredOrder() {
        var source = order(); when(mapper.selectDeliveryOrderPage(0, 10)).thenReturn(List.of(source));
        var view = service.queryDeliveryOrderPage(0, 10).get(0);
        assertNull(view.getPickupCode()); assertNull(view.getContactPhone());
        assertNull(view.getDeliveryAddress()); assertNull(view.getRemarks());
        assertEquals("public-id", view.getAuthorId()); assertEquals("公开昵称", view.getDisplayName());
        assertEquals(new BigDecimal("2.50"), view.getPrice()); assertEquals("合成驿站", view.getPickupLocation());
        assertEquals("synthetic-code", source.getPickupCode()); assertEquals("合成地址", source.getDeliveryAddress());
        assertTrue(service.queryDeliveryOrderPage(20, 10).isEmpty());
    }

    @Test void personalListsAndAuthorizedDetailKeepRequiredDeliveryFields() throws Exception {
        when(mapper.selectDeliveryOrderByUsername("owner")).thenReturn(List.of(order()));
        when(mapper.selectAcceptedDeliveryOrderByUsername("owner")).thenReturn(List.of(order()));
        when(mapper.selectDeliveryOrderByOrderId(7)).thenReturn(order());
        assertEquals("synthetic-code", service.queryPersonalDeliveryOrder("session").get(0).getPickupCode());
        assertEquals("合成地址", service.queryPersonalAcceptedDeliveryOrder("session").get(0).getDeliveryAddress());
        assertEquals("合成备注", service.queryDeliveryOrderByOrderId(7).getRemarks());
        assertThrows(DataNotExistException.class, () -> service.queryDeliveryOrderByOrderId(99));
        when(mapper.selectDeliveryOrderByUsername("owner")).thenReturn(null);
        assertTrue(service.queryPersonalDeliveryOrder("session").isEmpty());
    }

    @Test void tradeAccessDistinguishesPublisherAccepterAndUnrelatedViewer() throws Exception {
        var trade = new DeliveryTradeEntity(); trade.setTradeId(9); trade.setOrderId(7); trade.setUsername("runner");
        trade.setState(0); trade.setDeliveryOrder(order());
        when(mapper.selectDeliveryTradeByTradeId(9)).thenReturn(trade);
        when(mapper.selectDeliveryTradeByOrderId(7)).thenReturn(trade);
        when(mapper.selectDeliveryTradeByUsername("runner")).thenReturn(List.of(trade));
        when(mapper.selectDeliveryOrderUsernameByOrderId(7)).thenReturn("owner");
        assertEquals(0, service.queryDeliveryTradeDetailType("session", 9));
        when(certificates.getUserLoginCertificate("runner-session")).thenReturn(new User("runner"));
        assertEquals(2, service.queryDeliveryTradeDetailType("runner-session", 9));
        when(certificates.getUserLoginCertificate("visitor-session")).thenReturn(new User("visitor"));
        assertEquals(1, service.queryDeliveryTradeDetailType("visitor-session", 9));
        assertEquals("public-id", service.queryDeliveryTradeByOrderId(7).getAuthorId());
        assertEquals("公开昵称", service.queryDeliveryTradeByTradeId(9).getDisplayName());
        assertEquals(7, service.queryPersonalDeliveryTrade("runner").get(0).getOrderId());
        assertTrue(service.queryPersonalDeliveryTrade("absent").isEmpty());
        assertThrows(DataNotExistException.class, () -> service.queryDeliveryTradeByOrderId(99));
        assertThrows(DataNotExistException.class, () -> service.queryDeliveryTradeByTradeId(99));
    }

    @Test void orderPublicationUsesSessionOwnerAndDeletionDetectsAConcurrentStateChange() throws Exception {
        var dto = new DeliveryPublishDTO(); dto.setTaskName("合成任务"); dto.setPickupCode("synthetic-code");
        dto.setContactPhone("synthetic-phone"); dto.setPrice(new BigDecimal("2.50")); dto.setPickupLocation("合成驿站");
        dto.setDeliveryAddress("合成地址"); dto.setRemarks("合成备注");
        service.addDeliveryOrder("session", dto);
        var inserted = ArgumentCaptor.forClass(DeliveryOrderEntity.class); verify(mapper).insertDeliveryOrder(inserted.capture());
        assertEquals("owner", inserted.getValue().getUsername()); assertEquals("合成任务", inserted.getValue().getTaskName());
        assertEquals("synthetic-code", inserted.getValue().getPickupCode()); assertEquals("synthetic-phone", inserted.getValue().getContactPhone());
        assertEquals(new BigDecimal("2.50"), inserted.getValue().getPrice()); assertEquals("合成地址", inserted.getValue().getDeliveryAddress());
        when(mapper.selectDeliveryOrderByOrderId(7)).thenReturn(order()); when(mapper.deleteOrder(7)).thenReturn(1, 0);
        service.deleteOrder(7, "session");
        assertThrows(DeliveryOrderStateUpdatedException.class, () -> service.deleteOrder(7, "session"));
        assertThrows(DataNotExistException.class, () -> service.deleteOrder(99, "session"));
    }
}
