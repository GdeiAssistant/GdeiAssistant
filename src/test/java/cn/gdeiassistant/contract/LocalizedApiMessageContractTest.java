package cn.gdeiassistant.contract;

import cn.gdeiassistant.common.exception.QueryException.NotAvailableConditionException;
import cn.gdeiassistant.common.exceptionhandler.GlobalRestExceptionHandler;
import cn.gdeiassistant.core.i18n.BackendTextLocalizer;
import cn.gdeiassistant.core.message.controller.MessageController;
import cn.gdeiassistant.core.message.pojo.vo.InteractionMessageVO;
import cn.gdeiassistant.core.message.service.MessageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class LocalizedApiMessageContractTest {

    private MockMvc messageMockMvc;

    @BeforeEach
    void setUp() {
        MessageService messageService = mock(MessageService.class);

        InteractionMessageVO message = new InteractionMessageVO();
        message.setId("message-1");
        message.setModule("secret");
        message.setType("comment");
        message.setTitle("树洞收到新评论");
        message.setContent("alice 评论了你的树洞：一起加油");
        message.setCreatedAt("2026-03-20 20:30:00");
        message.setIsRead(false);
        message.setTargetType("comment");
        message.setTargetId("301");
        message.setTargetSubId("1");

        when(messageService.queryInteractionMessages("session-1", 0, 20))
                .thenReturn(List.of(message));

        MessageController messageController = new MessageController();
        ReflectionTestUtils.setField(messageController, "messageService", messageService);

        messageMockMvc = MockMvcBuilders.standaloneSetup(messageController).build();
    }

    @Test
    void interactionMessagesRespectEnglishLocale() throws Exception {
        messageMockMvc.perform(get("/api/information/message/interaction/start/0/size/20")
                        .requestAttr("sessionId", "session-1")
                        .header("Accept-Language", "en-US")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].title").value("Your confession received a new comment"))
                .andExpect(jsonPath("$.data[0].content").value("alice commented on your confession: 一起加油"));
    }

    @Test
    void exceptionMessagesRespectEnglishLocale() throws Exception {
        MockMvc exceptionMockMvc = MockMvcBuilders
                .standaloneSetup(new LocalizedExceptionTestController())
                .setControllerAdvice(new GlobalRestExceptionHandler())
                .build();

        exceptionMockMvc.perform(get("/api/test/localized-error")
                        .header("Accept-Language", "en-US")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Grades are not available for the current academic year"));
    }

    @Test
    void backendTextLocalizerCoversManualSuccessAndIntegrationMessages() {
        assertEquals("Schedule data refreshed successfully",
                BackendTextLocalizer.localizeMessage("课表数据更新成功", "en-US"));
        assertEquals("Thanks for your feedback",
                BackendTextLocalizer.localizeMessage("感谢您的反馈", "en-US"));
        assertEquals("The campus news site is temporarily unavailable",
                BackendTextLocalizer.localizeMessage("校园新闻站点访问失败", "en-US"));
        assertEquals("The academic system page structure has changed and the term selector could not be found",
                BackendTextLocalizer.localizeMessage("教务系统页面结构异常，未找到学期下拉框", "en-US"));
    }

    @RestController
    @RequestMapping("/api/test")
    static class LocalizedExceptionTestController {

        @GetMapping("/localized-error")
        public void localizedError() throws NotAvailableConditionException {
            throw new NotAvailableConditionException("当前学年暂不可查询");
        }
    }
}
