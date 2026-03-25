package cn.gdeiassistant.contract;

import cn.gdeiassistant.core.marketplace.controller.MarketplaceController;
import cn.gdeiassistant.core.marketplace.pojo.entity.MarketplaceItemEntity;
import cn.gdeiassistant.core.marketplace.pojo.vo.MarketplaceItemVO;
import cn.gdeiassistant.core.marketplace.service.MarketplaceService;
import cn.gdeiassistant.core.profile.pojo.vo.ProfileVO;
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
 * Contract tests for Marketplace endpoints.
 * Verifies that response shapes remain stable when backend logic changes.
 */
class MarketplaceContractTest {

    private MockMvc mockMvc;
    private MarketplaceService marketplaceService;

    @BeforeEach
    void setUp() {
        marketplaceService = mock(MarketplaceService.class);

        MarketplaceController controller = new MarketplaceController();
        ReflectionTestUtils.setField(controller, "marketplaceService", marketplaceService);

        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void listEndpointReturnsExpectedFields() throws Exception {
        when(marketplaceService.queryItems(0)).thenReturn(List.of(mockItemEntity()));

        mockMvc.perform(get("/api/ershou/item/start/0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].id").exists())
                .andExpect(jsonPath("$.data[0].name").exists())
                .andExpect(jsonPath("$.data[0].description").exists())
                .andExpect(jsonPath("$.data[0].price").exists())
                .andExpect(jsonPath("$.data[0].location").exists())
                .andExpect(jsonPath("$.data[0].type").exists())
                .andExpect(jsonPath("$.data[0].state").exists());
    }

    @Test
    void keywordSearchEndpointReturnsExpectedFields() throws Exception {
        when(marketplaceService.queryItemsWithKeyword("textbook", 0))
                .thenReturn(List.of(mockItemEntity()));

        mockMvc.perform(get("/api/ershou/keyword/textbook/start/0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].id").value(1))
                .andExpect(jsonPath("$.data[0].name").value("Textbook"));
    }

    @Test
    void keywordSearchReturnsEmptyArrayWhenNoResults() throws Exception {
        when(marketplaceService.queryItemsWithKeyword("noresult", 0)).thenReturn(List.of());

        mockMvc.perform(get("/api/ershou/keyword/noresult/start/0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    void typeFilterEndpointReturnsExpectedFields() throws Exception {
        when(marketplaceService.queryItemsByType(8, 0)).thenReturn(List.of(mockItemEntity()));

        mockMvc.perform(get("/api/ershou/item/type/8/start/0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].id").exists())
                .andExpect(jsonPath("$.data[0].type").value(8));
    }

    @Test
    void detailEndpointReturnsExpectedShape() throws Exception {
        MarketplaceItemVO vo = mockItemVO(1);

        when(marketplaceService.queryDetailById(1)).thenReturn(vo);

        mockMvc.perform(get("/api/ershou/item/id/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.secondhandItem.id").value(1))
                .andExpect(jsonPath("$.data.secondhandItem.name").value("Textbook"))
                .andExpect(jsonPath("$.data.secondhandItem.price").value(25.0))
                .andExpect(jsonPath("$.data.profile.nickname").exists());
    }

    @Test
    void previewEndpointReturnsSingleUrl() throws Exception {
        when(marketplaceService.getItemPictureURL(1)).thenReturn(List.of("https://example.com/pic1.jpg"));

        mockMvc.perform(get("/api/ershou/item/id/1/preview"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value("https://example.com/pic1.jpg"));
    }

    @Test
    void previewEndpointReturnsFailureWhenNoPictures() throws Exception {
        when(marketplaceService.getItemPictureURL(99)).thenReturn(List.of());

        mockMvc.perform(get("/api/ershou/item/id/99/preview"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false));
    }

    // ----------------------------------------------------------------
    // Helpers
    // ----------------------------------------------------------------

    private static MarketplaceItemEntity mockItemEntity() {
        MarketplaceItemEntity item = new MarketplaceItemEntity();
        item.setId(1);
        item.setName("Textbook");
        item.setDescription("Calculus textbook");
        item.setPrice(25.0f);
        item.setLocation("Library Gate");
        item.setType(8);
        item.setState(1);
        item.setUsername("testuser");
        return item;
    }

    private static MarketplaceItemVO mockItemVO(int id) {
        MarketplaceItemEntity item = new MarketplaceItemEntity();
        item.setId(id);
        item.setName("Textbook");
        item.setDescription("Calculus textbook");
        item.setPrice(25.0f);
        item.setLocation("Library Gate");
        item.setType(8);
        item.setState(1);
        item.setUsername("testuser");

        ProfileVO profile = new ProfileVO();
        profile.setUsername("testuser");
        profile.setNickname("Test User");

        MarketplaceItemVO vo = new MarketplaceItemVO();
        vo.setSecondhandItem(item);
        vo.setProfile(profile);
        return vo;
    }
}
