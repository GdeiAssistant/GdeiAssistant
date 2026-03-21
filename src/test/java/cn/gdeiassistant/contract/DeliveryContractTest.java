package cn.gdeiassistant.contract;

import cn.gdeiassistant.core.delivery.controller.DeliveryController;
import cn.gdeiassistant.core.delivery.pojo.vo.DeliveryOrderVO;
import cn.gdeiassistant.core.delivery.service.DeliveryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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

        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
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
    void detailEndpointReturnsExpectedFields() throws Exception {
        DeliveryOrderVO order = mockDeliveryOrderVO();
        order.setState(0);

        when(deliveryService.queryDeliveryOrderByOrderId(1)).thenReturn(order);
        when(deliveryService.queryDeliveryOrderDetailType("test-session", 1)).thenReturn(1);

        mockMvc.perform(get("/api/delivery/order/id/1")
                        .requestAttr("sessionId", "test-session"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.order").exists())
                .andExpect(jsonPath("$.data.order.orderId").exists())
                .andExpect(jsonPath("$.data.order.name").exists())
                .andExpect(jsonPath("$.data.order.number").exists())
                .andExpect(jsonPath("$.data.order.phone").exists())
                .andExpect(jsonPath("$.data.order.price").exists())
                .andExpect(jsonPath("$.data.order.company").exists())
                .andExpect(jsonPath("$.data.order.address").exists())
                .andExpect(jsonPath("$.data.order.state").exists())
                .andExpect(jsonPath("$.data.detailType").isNumber());
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
