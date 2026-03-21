package cn.gdeiassistant.contract;

import cn.gdeiassistant.core.lostandfound.controller.LostAndFoundController;
import cn.gdeiassistant.core.lostandfound.pojo.vo.LostAndFoundItemVO;
import cn.gdeiassistant.core.lostandfound.service.LostAndFoundService;
import cn.gdeiassistant.core.marketplace.controller.MarketplaceController;
import cn.gdeiassistant.core.marketplace.pojo.entity.MarketplaceItemEntity;
import cn.gdeiassistant.core.marketplace.service.MarketplaceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CommunitySummaryContractTest {

    private MockMvc mockMvc;
    private MarketplaceService marketplaceService;
    private LostAndFoundService lostAndFoundService;

    @BeforeEach
    void setUp() {
        marketplaceService = mock(MarketplaceService.class);
        lostAndFoundService = mock(LostAndFoundService.class);

        MarketplaceController marketplaceController = new MarketplaceController();
        ReflectionTestUtils.setField(marketplaceController, "marketplaceService", marketplaceService);

        LostAndFoundController lostAndFoundController = new LostAndFoundController();
        ReflectionTestUtils.setField(lostAndFoundController, "lostAndFoundService", lostAndFoundService);

        mockMvc = MockMvcBuilders.standaloneSetup(marketplaceController, lostAndFoundController).build();
    }

    @Test
    void secondhandProfileSummaryReturnsGroupedListsWithoutHeaderProfile() throws Exception {
        MarketplaceItemEntity doingItem = new MarketplaceItemEntity();
        doingItem.setId(101);
        doingItem.setUsername("20231234");
        doingItem.setName("九成新计算器");
        doingItem.setDescription("考试周自提");
        doingItem.setPrice(38.5f);
        doingItem.setLocation("海珠校区");
        doingItem.setType(8);
        doingItem.setQq("123456");
        doingItem.setPhone("13800138000");
        doingItem.setState(1);
        doingItem.setPictureURL(List.of("https://cdn.gdeiassistant.cn/ershou/101_1.jpg"));

        MarketplaceItemEntity soldItem = new MarketplaceItemEntity();
        soldItem.setId(102);
        soldItem.setUsername("20231234");
        soldItem.setName("二手风扇");
        soldItem.setDescription("宿舍清仓");
        soldItem.setPrice(25.0f);
        soldItem.setLocation("白云校区");
        soldItem.setType(5);
        soldItem.setQq("654321");
        soldItem.setPhone("13900139000");
        soldItem.setState(0);
        soldItem.setPictureURL(List.of("https://cdn.gdeiassistant.cn/ershou/102_1.jpg"));

        MarketplaceItemEntity offItem = new MarketplaceItemEntity();
        offItem.setId(103);
        offItem.setUsername("20231234");
        offItem.setName("旧教材");
        offItem.setDescription("已停止出售");
        offItem.setPrice(12.0f);
        offItem.setLocation("花都校区");
        offItem.setType(8);
        offItem.setQq("777888");
        offItem.setPhone("13700137000");
        offItem.setState(2);
        offItem.setPictureURL(List.of("https://cdn.gdeiassistant.cn/ershou/103_1.jpg"));

        when(marketplaceService.queryPersonalItems("session-1"))
                .thenReturn(List.of(doingItem, soldItem, offItem));

        mockMvc.perform(get("/api/ershou/profile")
                        .requestAttr("sessionId", "session-1"))
                .andExpect(status().isOk())
                .andExpect(content().json(
                        ContractResourceSupport.loadJson("contracts/community-ershou-summary.success.json"),
                        true
                ));
    }

    @Test
    void lostFoundProfileSummaryReturnsGroupedListsWithoutHeaderProfile() throws Exception {
        LostAndFoundItemVO lostItem = new LostAndFoundItemVO();
        lostItem.setId(201);
        lostItem.setUsername("20231234");
        lostItem.setName("校园卡");
        lostItem.setDescription("图书馆三楼遗失");
        lostItem.setLocation("图书馆三楼");
        lostItem.setItemType(1);
        lostItem.setLostType(0);
        lostItem.setQq("123456");
        lostItem.setWechat("gdei-user");
        lostItem.setPhone("13800138000");
        lostItem.setState(0);
        lostItem.setPictureURL(List.of("https://cdn.gdeiassistant.cn/lostandfound/201_1.jpg"));

        LostAndFoundItemVO foundItem = new LostAndFoundItemVO();
        foundItem.setId(202);
        foundItem.setUsername("20231234");
        foundItem.setName("钥匙");
        foundItem.setDescription("教学楼 A 栋拾获");
        foundItem.setLocation("教学楼 A 栋");
        foundItem.setItemType(5);
        foundItem.setLostType(1);
        foundItem.setQq("456789");
        foundItem.setWechat("campus-helper");
        foundItem.setPhone("13900139000");
        foundItem.setState(0);
        foundItem.setPictureURL(List.of("https://cdn.gdeiassistant.cn/lostandfound/202_1.jpg"));

        LostAndFoundItemVO didFoundItem = new LostAndFoundItemVO();
        didFoundItem.setId(203);
        didFoundItem.setUsername("20231234");
        didFoundItem.setName("身份证");
        didFoundItem.setDescription("已完成认领");
        didFoundItem.setLocation("行政楼");
        didFoundItem.setItemType(2);
        didFoundItem.setLostType(0);
        didFoundItem.setQq("987654");
        didFoundItem.setWechat("gdei-found");
        didFoundItem.setPhone("13700137000");
        didFoundItem.setState(1);
        didFoundItem.setPictureURL(List.of("https://cdn.gdeiassistant.cn/lostandfound/203_1.jpg"));

        when(lostAndFoundService.queryPersonalLostAndFoundItems("session-1"))
                .thenReturn(List.of(lostItem, foundItem, didFoundItem));

        mockMvc.perform(get("/api/lostandfound/profile")
                        .requestAttr("sessionId", "session-1"))
                .andExpect(status().isOk())
                .andExpect(content().json(
                        ContractResourceSupport.loadJson("contracts/community-lostfound-summary.success.json"),
                        true
                ));
    }
}
