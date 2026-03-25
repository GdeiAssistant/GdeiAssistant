package cn.gdeiassistant.contract;

import cn.gdeiassistant.common.pojo.Entity.IPAddressRecord;
import cn.gdeiassistant.core.iPAddress.controller.IPAddressController;
import cn.gdeiassistant.core.iPAddress.service.IPAddressService;
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

/**
 * Contract tests for IP address / area endpoints.
 * After IPAddressMapper cleanup, these tests ensure the response shape
 * returned to clients (IP history and area) remains stable.
 */
class IPAddressContractTest {

    private MockMvc mockMvc;
    private IPAddressService ipAddressService;

    @BeforeEach
    void setUp() {
        ipAddressService = mock(IPAddressService.class);

        IPAddressController controller = new IPAddressController();
        ReflectionTestUtils.setField(controller, "ipAddressService", ipAddressService);

        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void areaEndpointReturnsProvinceForChineseMainlandIp() throws Exception {
        IPAddressRecord record = new IPAddressRecord();
        record.setCountry("中国");
        record.setProvince("广东省");
        record.setCity("广州市");
        record.setArea("广东省");

        when(ipAddressService.getSelfUserLatestPostTypeIPAddress("test-session"))
                .thenReturn(record);

        mockMvc.perform(get("/api/ip/area")
                        .requestAttr("sessionId", "test-session"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value("广东省"));
    }

    @Test
    void areaEndpointReturnsDashWhenNoRecord() throws Exception {
        IPAddressRecord empty = new IPAddressRecord();
        empty.setArea("-");

        when(ipAddressService.getSelfUserLatestPostTypeIPAddress("no-session"))
                .thenReturn(empty);

        mockMvc.perform(get("/api/ip/area")
                        .requestAttr("sessionId", "no-session"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value("-"));
    }

    @Test
    void historyEndpointReturnsExpectedFields() throws Exception {
        IPAddressRecord record = new IPAddressRecord();
        record.setIp("1.2.3.4");
        record.setCountry("中国");
        record.setProvince("广东省");
        record.setCity("广州市");

        when(ipAddressService.getSelfUserAddressRecord("test-session", 0, 0, 10))
                .thenReturn(List.of(record));

        mockMvc.perform(get("/api/ip/start/0/size/10")
                        .requestAttr("sessionId", "test-session"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].ip").value("1.2.3.4"))
                .andExpect(jsonPath("$.data[0].country").value("中国"))
                .andExpect(jsonPath("$.data[0].province").value("广东省"));
    }

    @Test
    void historyEndpointReturnsEmptyArrayWhenNoRecords() throws Exception {
        when(ipAddressService.getSelfUserAddressRecord("test-session", 0, 0, 10))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/ip/start/0/size/10")
                        .requestAttr("sessionId", "test-session"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isEmpty());
    }
}
