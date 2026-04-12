package cn.gdeiassistant.contract;

import cn.gdeiassistant.common.exceptionhandler.GlobalRestExceptionHandler;
import cn.gdeiassistant.common.pojo.Entity.ElectricFees;
import cn.gdeiassistant.core.dataquery.controller.DataQueryController;
import cn.gdeiassistant.core.dataquery.service.DataQueryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class DataQueryContractTest {

    private MockMvc mockMvc;
    private DataQueryService dataQueryService;

    @BeforeEach
    void setUp() {
        dataQueryService = mock(DataQueryService.class);

        DataQueryController controller = new DataQueryController();
        ReflectionTestUtils.setField(controller, "dataQueryService", dataQueryService);

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalRestExceptionHandler())
                .build();
    }

    @Test
    void electricFeesReturnsExpectedFields() throws Exception {
        ElectricFees fees = new ElectricFees();
        fees.setName("林知远");
        fees.setNumber(20231234567L);
        fees.setYear(2026);
        fees.setTotalElectricBill(32.5f);

        when(dataQueryService.queryElectricFees("林知远", 20231234567L, 2026)).thenReturn(fees);

        mockMvc.perform(post("/api/data/electricfees")
                        .param("name", "林知远")
                        .param("number", "20231234567")
                        .param("year", "2026"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("林知远"))
                .andExpect(jsonPath("$.data.number").value(20231234567L))
                .andExpect(jsonPath("$.data.year").value(2026))
                .andExpect(jsonPath("$.data.totalElectricBill").value(32.5));
    }

    @Test
    void electricFeesRejectsInvalidYearBounds() throws Exception {
        mockMvc.perform(post("/api/data/electricfees")
                        .param("name", "林知远")
                        .param("number", "20231234567")
                        .param("year", "2015"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false));

        verifyNoInteractions(dataQueryService);
    }

    @Test
    void electricFeesRejectsMissingNumberBeforeService() throws Exception {
        mockMvc.perform(post("/api/data/electricfees")
                        .param("name", "林知远")
                        .param("year", "2026"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false));

        verifyNoInteractions(dataQueryService);
    }
}
