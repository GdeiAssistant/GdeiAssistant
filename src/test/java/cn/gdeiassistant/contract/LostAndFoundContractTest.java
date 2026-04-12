package cn.gdeiassistant.contract;

import cn.gdeiassistant.common.exceptionhandler.GlobalRestExceptionHandler;
import cn.gdeiassistant.core.lostandfound.controller.LostAndFoundController;
import cn.gdeiassistant.core.lostandfound.pojo.vo.LostAndFoundDetailVO;
import cn.gdeiassistant.core.lostandfound.pojo.vo.LostAndFoundItemVO;
import cn.gdeiassistant.core.lostandfound.service.LostAndFoundService;
import cn.gdeiassistant.core.profile.pojo.vo.ProfileVO;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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

    @Test
    void listEndpointsRejectNegativeStart() throws Exception {
        mockMvc.perform(get("/api/lostandfound/lostitem/start/-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false));

        mockMvc.perform(get("/api/lostandfound/founditem/start/-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false));

        verifyNoInteractions(lostAndFoundService);
    }

    @Test
    void lostSearchEndpointRejectsInvalidTypeOrStart() throws Exception {
        mockMvc.perform(post("/api/lostandfound/lostitem/type/2/start/0")
                        .param("keyword", "校园卡"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false));

        mockMvc.perform(post("/api/lostandfound/lostitem/type/0/start/-1")
                        .param("keyword", "校园卡"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false));

        verifyNoInteractions(lostAndFoundService);
    }

    @Test
    void detailEndpointReturnsExpectedShape() throws Exception {
        LostAndFoundDetailVO detail = new LostAndFoundDetailVO();
        LostAndFoundItemVO item = mockLostAndFoundItem();
        ProfileVO profile = new ProfileVO();
        profile.setUsername("testuser");
        profile.setNickname("Test User");
        detail.setItem(item);
        detail.setProfile(profile);

        when(lostAndFoundService.queryLostAndFoundInfoByID(1)).thenReturn(detail);

        mockMvc.perform(get("/api/lostandfound/item/id/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.item.id").value(1))
                .andExpect(jsonPath("$.data.item.name").value("campus card"))
                .andExpect(jsonPath("$.data.profile.nickname").value("Test User"));
    }

    @Test
    void previewEndpointReturnsFailureWhenNoPictures() throws Exception {
        when(lostAndFoundService.getLostAndFoundItemPictureURL(99)).thenReturn(List.of());

        mockMvc.perform(get("/api/lostandfound/item/id/99/preview"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false));
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
