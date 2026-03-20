package cn.gdeiassistant.contract;

import cn.gdeiassistant.common.exception.DatabaseException.DataNotExistException;
import cn.gdeiassistant.core.announcement.controller.AnnouncementController;
import cn.gdeiassistant.core.information.pojo.vo.AnnouncementVO;
import cn.gdeiassistant.core.information.service.Announcement.AnnouncementService;
import cn.gdeiassistant.core.information.service.SchoolNews.SchoolNewsService;
import cn.gdeiassistant.core.message.controller.MessageController;
import cn.gdeiassistant.core.message.service.MessageService;
import cn.gdeiassistant.core.schoolNews.controller.SchoolNewsController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.text.SimpleDateFormat;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class InformationCenterContractTest {

    private MockMvc mockMvc;
    private SchoolNewsService schoolNewsService;
    private AnnouncementService announcementService;
    private MessageService messageService;

    @BeforeEach
    void setUp() {
        schoolNewsService = mock(SchoolNewsService.class);
        announcementService = mock(AnnouncementService.class);
        messageService = mock(MessageService.class);

        SchoolNewsController schoolNewsController = new SchoolNewsController();
        ReflectionTestUtils.setField(schoolNewsController, "schoolNewsService", schoolNewsService);

        AnnouncementController announcementController = new AnnouncementController();
        ReflectionTestUtils.setField(announcementController, "announcementService", announcementService);

        MessageController messageController = new MessageController();
        ReflectionTestUtils.setField(messageController, "messageService", messageService);

        mockMvc = MockMvcBuilders.standaloneSetup(
                schoolNewsController,
                announcementController,
                messageController
        ).build();
    }

    @Test
    void newsListReturnsEmptyPayloadWhenSourceHasNoData() throws Exception {
        when(schoolNewsService.queryNewInfoList(1, 0, 10))
                .thenThrow(new DataNotExistException("no data"));

        mockMvc.perform(get("/api/information/news/type/1/start/0/size/10"))
                .andExpect(status().isOk())
                .andExpect(content().json(
                        ContractResourceSupport.loadJson("contracts/information-news.empty.json")
                ));
    }

    @Test
    void announcementListReturnsCanonicalPayload() throws Exception {
        AnnouncementVO announcement = new AnnouncementVO();
        announcement.setId("announcement-1");
        announcement.setTitle("系统维护通知");
        announcement.setContent("信息中心将于今晚 23:00 维护。");
        announcement.setPublishTime(new SimpleDateFormat("yyyy-MM-dd").parse("2026-03-20"));

        when(announcementService.queryAnnouncementPage(0, 10))
                .thenReturn(List.of(announcement));

        mockMvc.perform(get("/api/information/announcement/start/0/size/10"))
                .andExpect(status().isOk())
                .andExpect(content().json(
                        ContractResourceSupport.loadJson("contracts/information-announcements.success.json")
                ));
    }

    @Test
    void unreadCountReturnsCanonicalPayload() throws Exception {
        when(messageService.queryInteractionUnreadCount("session-1")).thenReturn(3);

        mockMvc.perform(get("/api/information/message/unread")
                        .requestAttr("sessionId", "session-1"))
                .andExpect(status().isOk())
                .andExpect(content().json(
                        ContractResourceSupport.loadJson("contracts/information-message-unread.success.json")
                ));
    }
}
