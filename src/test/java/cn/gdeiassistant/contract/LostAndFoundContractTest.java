package cn.gdeiassistant.contract;

import cn.gdeiassistant.common.exceptionhandler.GlobalRestExceptionHandler;
import cn.gdeiassistant.core.lostandfound.controller.LostAndFoundController;
import cn.gdeiassistant.core.lostandfound.pojo.vo.LostAndFoundItemVO;
import cn.gdeiassistant.core.lostandfound.service.LostAndFoundService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class LostAndFoundContractTest {

    private MockMvc mockMvc;
    private LostAndFoundService lostAndFoundService;

    @BeforeEach
    void setUp() {
        lostAndFoundService = mock(LostAndFoundService.class);

        LostAndFoundController controller = new LostAndFoundController();
        ReflectionTestUtils.setField(controller, "lostAndFoundService", lostAndFoundService);

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalRestExceptionHandler())
                .build();
    }

    @Test
    void foundTypeEndpointReturnsExpectedFields() throws Exception {
        when(lostAndFoundService.queryFoundItemsByType(3, 0))
                .thenReturn(List.of(mockLostAndFoundItem()));

        mockMvc.perform(get("/api/lostandfound/founditem/type/3/start/0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].id").value(1))
                .andExpect(jsonPath("$.data[0].name").value("campus card"))
                .andExpect(jsonPath("$.data[0].itemType").value(3));
    }

    @Test
    void foundTypeEndpointRejectsInvalidTypeOrStart() throws Exception {
        mockMvc.perform(get("/api/lostandfound/founditem/type/12/start/0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false));

        mockMvc.perform(get("/api/lostandfound/founditem/type/3/start/-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false));

        verifyNoInteractions(lostAndFoundService);
    }

    private static LostAndFoundItemVO mockLostAndFoundItem() {
        LostAndFoundItemVO item = new LostAndFoundItemVO();
        item.setId(1);
        item.setName("campus card");
        item.setDescription("found near library");
        item.setLocation("Library");
        item.setItemType(3);
        item.setLostType(1);
        item.setState(0);
        return item;
    }
}
