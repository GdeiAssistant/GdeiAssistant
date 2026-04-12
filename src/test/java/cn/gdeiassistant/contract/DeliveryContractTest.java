package cn.gdeiassistant.contract;

import cn.gdeiassistant.common.exceptionhandler.GlobalRestExceptionHandler;
import cn.gdeiassistant.core.delivery.controller.DeliveryController;
import cn.gdeiassistant.core.delivery.pojo.dto.DeliveryPublishDTO;
import cn.gdeiassistant.core.delivery.pojo.vo.DeliveryOrderVO;
import cn.gdeiassistant.core.delivery.service.DeliveryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class DeliveryContractTest {

    private MockMvc mockMvc;
    private DeliveryService deliveryService;

    @BeforeEach
    void setUp() {
        deliveryService = mock(DeliveryService.class);

        DeliveryController controller = new DeliveryController();
        ReflectionTestUtils.setField(controller, "deliveryService", deliveryService);

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalRestExceptionHandler())
                .build();
    }

    @Test
    void listEndpointReturnsExpectedFields() throws Exception {
        when(deliveryService.queryDeliveryOrderPage(0, 10))
                .thenReturn(List.of(mockDeliveryOrderVO()));

        mockMvc.perform(get("/api/delivery/order/start/0/size/10")
                        .requestAttr("sessionId", "test-session"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].orderId").exists())
                .andExpect(jsonPath("$.data[0].name").exists())
                .andExpect(jsonPath("$.data[0].number").exists())
                .andExpect(jsonPath("$.data[0].phone").exists())
                .andExpect(jsonPath("$.data[0].price").exists())
                .andExpect(jsonPath("$.data[0].company").exists())
                .andExpect(jsonPath("$.data[0].address").exists())
                .andExpect(jsonPath("$.data[0].state").exists());
    }

    @Test
    void listEndpointCapsPageSizeAtFifty() throws Exception {
        when(deliveryService.queryDeliveryOrderPage(0, 50))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/delivery/order/start/0/size/100")
                        .requestAttr("sessionId", "test-session"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(deliveryService).queryDeliveryOrderPage(0, 50);
    }

    @Test
    void listEndpointRejectsLowerBoundViolations() throws Exception {
        mockMvc.perform(get("/api/delivery/order/start/-1/size/10")
                        .requestAttr("sessionId", "test-session"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false));

        mockMvc.perform(get("/api/delivery/order/start/0/size/0")
                        .requestAttr("sessionId", "test-session"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false));

        verifyNoInteractions(deliveryService);
    }

    @Test
    void acceptOrderAcceptsValidOrderId() throws Exception {
        mockMvc.perform(post("/api/delivery/acceptorder")
                        .requestAttr("sessionId", "test-session")
                        .param("orderId", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(deliveryService).acceptOrder(10, "test-session");
    }

    @Test
    void acceptOrderRejectsInvalidOrderIdBeforeService() throws Exception {
        mockMvc.perform(post("/api/delivery/acceptorder")
                        .requestAttr("sessionId", "test-session")
                        .param("orderId", "0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false));

        verifyNoInteractions(deliveryService);
    }

    @Test
    void deleteOrderAcceptsValidOrderId() throws Exception {
        mockMvc.perform(delete("/api/delivery/order/id/10")
                        .requestAttr("sessionId", "test-session"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(deliveryService).deleteOrder(10, "test-session");
    }

    @Test
    void deleteOrderRejectsInvalidOrderIdBeforeService() throws Exception {
        mockMvc.perform(delete("/api/delivery/order/id/0")
                        .requestAttr("sessionId", "test-session"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false));

        verifyNoInteractions(deliveryService);
    }

    @Test
    void finishTradeAcceptsValidTradeId() throws Exception {
        mockMvc.perform(post("/api/delivery/trade/id/10/finishtrade")
                        .requestAttr("sessionId", "test-session"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(deliveryService).finishTrade(10, "test-session");
    }

    @Test
    void finishTradeRejectsInvalidTradeIdBeforeService() throws Exception {
        mockMvc.perform(post("/api/delivery/trade/id/0/finishtrade")
                        .requestAttr("sessionId", "test-session"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false));

        verifyNoInteractions(deliveryService);
    }

    @Test
    void detailEndpointRejectsInvalidOrderIdBeforeService() throws Exception {
        mockMvc.perform(get("/api/delivery/order/id/0")
                        .requestAttr("sessionId", "test-session"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false));

        verifyNoInteractions(deliveryService);
    }

    @Test
    void publishEndpointAcceptsValidPayload() throws Exception {
        mockMvc.perform(validPublishRequestBuilder())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        ArgumentCaptor<DeliveryPublishDTO> captor = ArgumentCaptor.forClass(DeliveryPublishDTO.class);
        verify(deliveryService).addDeliveryOrder(eq("test-session"), captor.capture());
        DeliveryPublishDTO dto = captor.getValue();
        assertEquals("代收", dto.getName());
        assertEquals("00000000000", dto.getNumber());
        assertEquals("13800138000", dto.getPhone());
        assertEquals(5.50f, dto.getPrice());
        assertEquals("菜鸟驿站", dto.getCompany());
        assertEquals("南苑5栋307", dto.getAddress());
        assertEquals("轻拿轻放", dto.getRemarks());
    }

    @Test
    void publishEndpointRejectsMissingRequiredFieldsBeforeService() throws Exception {
        mockMvc.perform(post("/api/delivery/order")
                        .requestAttr("sessionId", "test-session")
                        .param("name", "")
                        .param("number", "00000000000")
                        .param("phone", "13800138000")
                        .param("price", "5.50")
                        .param("company", "菜鸟驿站")
                        .param("address", "南苑5栋307"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false));

        verifyNoInteractions(deliveryService);
    }

    @Test
    void publishEndpointRejectsInvalidNumberOrPhoneBeforeService() throws Exception {
        mockMvc.perform(publishRequestBuilder("代收", "1234567890", "13800138000", "5.50", "菜鸟驿站", "南苑5栋307", "轻拿轻放"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false));

        mockMvc.perform(publishRequestBuilder("代收", "00000000000", "138001380001", "5.50", "菜鸟驿站", "南苑5栋307", "轻拿轻放"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false));

        verifyNoInteractions(deliveryService);
    }

    @Test
    void publishEndpointRejectsInvalidPriceOrRemarksBeforeService() throws Exception {
        mockMvc.perform(publishRequestBuilder("代收", "00000000000", "13800138000", "0", "菜鸟驿站", "南苑5栋307", "轻拿轻放"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false));

        mockMvc.perform(publishRequestBuilder("代收", "00000000000", "13800138000", "10000.00", "菜鸟驿站", "南苑5栋307", "轻拿轻放"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false));

        mockMvc.perform(publishRequestBuilder("代收", "00000000000", "13800138000", "5.50", "菜鸟驿站", "南苑5栋307", "x".repeat(101)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false));

        verifyNoInteractions(deliveryService);
    }

    @Test
    void detailEndpointReturnsFullFieldsForOwner() throws Exception {
        DeliveryOrderVO order = mockDeliveryOrderVO();
        order.setState(0);

        when(deliveryService.queryDeliveryOrderByOrderId(1)).thenReturn(order);
        when(deliveryService.queryDeliveryOrderDetailType("test-session", 1)).thenReturn(0);

        mockMvc.perform(get("/api/delivery/order/id/1")
                        .requestAttr("sessionId", "test-session"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.order.orderId").exists())
                .andExpect(jsonPath("$.data.order.number").value("SF123456"))
                .andExpect(jsonPath("$.data.order.phone").value("13800138000"))
                .andExpect(jsonPath("$.data.order.price").exists())
                .andExpect(jsonPath("$.data.detailType").value(0));
    }

    @Test
    void detailEndpointStripsSensitiveFieldsForThirdParty() throws Exception {
        DeliveryOrderVO order = mockDeliveryOrderVO();
        order.setState(0);

        when(deliveryService.queryDeliveryOrderByOrderId(1)).thenReturn(order);
        when(deliveryService.queryDeliveryOrderDetailType("test-session", 1)).thenReturn(1);

        mockMvc.perform(get("/api/delivery/order/id/1")
                        .requestAttr("sessionId", "test-session"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.order.orderId").exists())
                .andExpect(jsonPath("$.data.order.number").doesNotExist())
                .andExpect(jsonPath("$.data.order.phone").doesNotExist())
                .andExpect(jsonPath("$.data.detailType").value(1));
    }

    private static MockHttpServletRequestBuilder validPublishRequestBuilder() {
        return publishRequestBuilder("代收", "00000000000", "13800138000", "5.50", "菜鸟驿站", "南苑5栋307", "轻拿轻放");
    }

    private static MockHttpServletRequestBuilder publishRequestBuilder(String name,
            String number, String phone, String price, String company, String address, String remarks) {
        return post("/api/delivery/order")
                .requestAttr("sessionId", "test-session")
                .param("name", name)
                .param("number", number)
                .param("phone", phone)
                .param("price", price)
                .param("company", company)
                .param("address", address)
                .param("remarks", remarks);
    }

    private static DeliveryOrderVO mockDeliveryOrderVO() {
        DeliveryOrderVO vo = new DeliveryOrderVO();
        vo.setOrderId(1);
        vo.setName("Zhang San");
        vo.setNumber("SF123456");
        vo.setPhone("13800138000");
        vo.setPrice(5.0f);
        vo.setCompany("Shunfeng");
        vo.setAddress("Building A");
        vo.setState(0);
        return vo;
    }
}
