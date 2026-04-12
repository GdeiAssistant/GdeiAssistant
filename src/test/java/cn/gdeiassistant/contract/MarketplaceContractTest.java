package cn.gdeiassistant.contract;

import cn.gdeiassistant.common.exceptionhandler.GlobalRestExceptionHandler;
import cn.gdeiassistant.core.marketplace.controller.MarketplaceController;
import cn.gdeiassistant.core.marketplace.pojo.dto.MarketplacePublishDTO;
import cn.gdeiassistant.core.marketplace.pojo.entity.MarketplaceItemEntity;
import cn.gdeiassistant.core.marketplace.pojo.vo.MarketplaceItemVO;
import cn.gdeiassistant.core.marketplace.service.MarketplaceService;
import cn.gdeiassistant.core.profile.pojo.vo.ProfileVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalRestExceptionHandler())
                .build();
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
    void listEndpointRejectsNegativeStart() throws Exception {
        mockMvc.perform(get("/api/ershou/item/start/-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false));

        mockMvc.perform(get("/api/ershou/keyword/textbook/start/-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false));

        verifyNoInteractions(marketplaceService);
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
    void typeFilterEndpointRejectsInvalidTypeOrStart() throws Exception {
        mockMvc.perform(get("/api/ershou/item/type/12/start/0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false));

        mockMvc.perform(get("/api/ershou/item/type/8/start/-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false));

        verifyNoInteractions(marketplaceService);
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

    @Test
    void stateEndpointUpdatesValidState() throws Exception {
        mockMvc.perform(post("/api/ershou/item/state/id/101")
                        .requestAttr("sessionId", "test-session")
                        .param("state", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(marketplaceService).updateItemState("test-session", 101, 2);
    }

    @Test
    void stateEndpointRejectsInvalidStateBeforeService() throws Exception {
        mockMvc.perform(post("/api/ershou/item/state/id/101")
                        .requestAttr("sessionId", "test-session")
                        .param("state", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false));

        verifyNoInteractions(marketplaceService);
    }


    @Test
    void publishEndpointAcceptsValidPayloadAndImageKeys() throws Exception {
        MarketplaceItemEntity saved = mockItemEntity();
        saved.setId(6);
        when(marketplaceService.publishItem(any(MarketplacePublishDTO.class), eq("test-session")))
                .thenReturn(saved);

        mockMvc.perform(post("/api/ershou/item")
                        .requestAttr("sessionId", "test-session")
                        .param("name", "教材")
                        .param("description", "九成新微积分教材")
                        .param("price", "25.50")
                        .param("location", "图书馆门口")
                        .param("type", "8")
                        .param("qq", "123456")
                        .param("phone", "13612340001")
                        .param("imageKeys", "upload/item-1.jpg", "upload/item-2.jpg"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        ArgumentCaptor<MarketplacePublishDTO> captor = ArgumentCaptor.forClass(MarketplacePublishDTO.class);
        verify(marketplaceService).publishItem(captor.capture(), eq("test-session"));
        MarketplacePublishDTO dto = captor.getValue();
        assertEquals("教材", dto.getName());
        assertEquals("九成新微积分教材", dto.getDescription());
        assertEquals(Float.valueOf(25.50f), dto.getPrice());
        assertEquals("图书馆门口", dto.getLocation());
        assertEquals(8, dto.getType());
        assertEquals("123456", dto.getQq());
        assertEquals("13612340001", dto.getPhone());
        verify(marketplaceService).moveItemPictureFromTempObject(6, 1, "upload/item-1.jpg");
        verify(marketplaceService).moveItemPictureFromTempObject(6, 2, "upload/item-2.jpg");
    }

    @Test
    void publishEndpointAcceptsInclusivePriceBounds() throws Exception {
        MarketplaceItemEntity minSaved = mockItemEntity();
        minSaved.setId(7);
        MarketplaceItemEntity maxSaved = mockItemEntity();
        maxSaved.setId(8);
        when(marketplaceService.publishItem(any(MarketplacePublishDTO.class), eq("test-session")))
                .thenReturn(minSaved, maxSaved);

        mockMvc.perform(post("/api/ershou/item")
                        .requestAttr("sessionId", "test-session")
                        .param("name", "贴纸")
                        .param("description", "边界价格测试")
                        .param("price", "0.01")
                        .param("location", "图书馆门口")
                        .param("type", "8")
                        .param("imageKeys", "upload/min-price.jpg"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        mockMvc.perform(post("/api/ershou/item")
                        .requestAttr("sessionId", "test-session")
                        .param("name", "电脑")
                        .param("description", "边界价格测试")
                        .param("price", "9999.99")
                        .param("location", "图书馆门口")
                        .param("type", "8")
                        .param("imageKeys", "upload/max-price.jpg"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(marketplaceService).moveItemPictureFromTempObject(7, 1, "upload/min-price.jpg");
        verify(marketplaceService).moveItemPictureFromTempObject(8, 1, "upload/max-price.jpg");
    }

    @Test
    void publishEndpointRejectsInvalidPriceOrTypeBeforeService() throws Exception {
        mockMvc.perform(post("/api/ershou/item")
                        .requestAttr("sessionId", "test-session")
                        .param("name", "教材")
                        .param("description", "九成新微积分教材")
                        .param("location", "图书馆门口")
                        .param("type", "8")
                        .param("imageKeys", "upload/item-1.jpg"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false));

        mockMvc.perform(post("/api/ershou/item")
                        .requestAttr("sessionId", "test-session")
                        .param("name", "教材")
                        .param("description", "九成新微积分教材")
                        .param("price", "0")
                        .param("location", "图书馆门口")
                        .param("type", "8")
                        .param("imageKeys", "upload/item-1.jpg"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false));

        mockMvc.perform(post("/api/ershou/item")
                        .requestAttr("sessionId", "test-session")
                        .param("name", "教材")
                        .param("description", "九成新微积分教材")
                        .param("price", "25.50")
                        .param("location", "图书馆门口")
                        .param("imageKeys", "upload/item-1.jpg"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false));

        verifyNoInteractions(marketplaceService);
    }

    @Test
    void publishEndpointRejectsInvalidImageKeysBeforeService() throws Exception {
        mockMvc.perform(post("/api/ershou/item")
                        .requestAttr("sessionId", "test-session")
                        .param("name", "教材")
                        .param("description", "九成新微积分教材")
                        .param("price", "25.50")
                        .param("location", "图书馆门口")
                        .param("type", "8")
                        .param("imageKeys", "upload/item-1.jpg", ""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false));

        mockMvc.perform(post("/api/ershou/item")
                        .requestAttr("sessionId", "test-session")
                        .param("name", "教材")
                        .param("description", "九成新微积分教材")
                        .param("price", "25.50")
                        .param("location", "图书馆门口")
                        .param("type", "8")
                        .param("imageKeys", "upload/item-1.jpg", "upload/item-2.jpg", "upload/item-3.jpg",
                                "upload/item-4.jpg", "upload/item-5.jpg"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false));

        mockMvc.perform(post("/api/ershou/item")
                        .requestAttr("sessionId", "test-session")
                        .param("name", "教材")
                        .param("description", "九成新微积分教材")
                        .param("price", "25.50")
                        .param("location", "图书馆门口")
                        .param("type", "8"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false));

        verifyNoInteractions(marketplaceService);
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
