package cn.gdeiassistant.core.feedback.controller;

import cn.gdeiassistant.common.exceptionhandler.GlobalRestExceptionHandler;
import cn.gdeiassistant.common.pojo.entity.User;
import cn.gdeiassistant.common.tools.springutils.EmailUtils;
import cn.gdeiassistant.core.feedback.service.FeedbackService;
import cn.gdeiassistant.core.feedback.mapper.FeedbackMapper;
import cn.gdeiassistant.core.feedback.pojo.entity.FeedbackEntity;
import cn.gdeiassistant.core.userlogin.service.UserCertificateService;
import org.junit.jupiter.api.*;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.mock.web.MockMultipartFile;
import java.io.InputStream;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class FeedbackHttpFlowTest {
    private final FeedbackMapper database=mock(FeedbackMapper.class);
    private final EmailUtils email=mock(EmailUtils.class);
    private FeedbackService service;private MockMvc http;
    @BeforeEach void setup(){
        var certificate=mock(UserCertificateService.class);when(certificate.getUserLoginCertificate("synthetic-session")).thenReturn(new User("synthetic-owner"));
        service=new FeedbackService();ReflectionTestUtils.setField(service,"feedbackMapper",database);ReflectionTestUtils.setField(service,"userCertificateService",certificate);ReflectionTestUtils.setField(service,"emailUtils",email);
        service.setSenderEmail("sender@example.invalid");service.setFeedbackEmail("feedback@example.invalid");service.setTicketEmail("ticket@example.invalid");
        var controller=new FeedbackController();ReflectionTestUtils.setField(controller,"feedbackService",service);
        http=MockMvcBuilders.standaloneSetup(controller).setControllerAdvice(new GlobalRestExceptionHandler()).build();
    }
    @Test void jsonFeedbackTrimsContentAndNeverSendsEmail() throws Exception {
        http.perform(post("/api/feedback").requestAttr("sessionId","synthetic-session").contentType("application/json").content("{\"content\":\"  合成反馈  \",\"contact\":\" synthetic \",\"type\":\" data \"}"))
            .andExpect(jsonPath("$.success").value(true));
        var entity=ArgumentCaptor.forClass(FeedbackEntity.class);verify(database).insertFeedback(entity.capture());
        assertEquals("synthetic-owner",entity.getValue().getUsername());assertEquals("合成反馈",entity.getValue().getContent());assertEquals("synthetic",entity.getValue().getContact());assertEquals("data",entity.getValue().getType());verifyNoInteractions(email);
        http.perform(post("/api/feedback").requestAttr("sessionId","synthetic-session").contentType("application/json").content("{\"content\":\"  \"}"))
            .andExpect(jsonPath("$.success").value(false));verify(database,times(1)).insertFeedback(any());
        http.perform(post("/api/feedback").requestAttr("sessionId","synthetic-session").contentType("application/json").content("{\"content\":\"optional fields absent\"}"))
            .andExpect(jsonPath("$.success").value(true));
    }
    @Test void multipartRoutingAndAttachmentsAreDistinctFromDatabaseFeedback() throws Exception {
        var file=new MockMultipartFile("images","synthetic.png","image/png",new byte[]{1,2,3});
        http.perform(multipart("/api/feedback/function").file(file).requestAttr("sessionId","synthetic-session").param("content","synthetic feedback"))
            .andExpect(jsonPath("$.success").value(true));
        var streams=ArgumentCaptor.forClass(InputStream[].class);
        verify(email).sendEmail(eq("sender@example.invalid"),eq("feedback@example.invalid"),contains("意见建议反馈"),eq("synthetic feedback"),streams.capture());
        assertArrayEquals(new byte[]{1,2,3},streams.getValue()[0].readAllBytes());streams.getValue()[0].close();
        http.perform(multipart("/api/feedback/ticket").requestAttr("sessionId","synthetic-session").param("content","synthetic ticket").param("type","network"))
            .andExpect(jsonPath("$.success").value(true));
        verify(email).sendEmail(eq("sender@example.invalid"),eq("ticket@example.invalid"),contains("network分类故障工单"),eq("synthetic ticket"),argThat(a->a.length==0));verifyNoInteractions(database);
    }
    @Test void invalidAttachmentsAndDisabledMailCannotReportSuccess() throws Exception {
        var empty=new MockMultipartFile("images","empty.png","image/png",new byte[0]);
        http.perform(multipart("/api/feedback/function").file(empty).requestAttr("sessionId","synthetic-session").param("content","synthetic"))
            .andExpect(jsonPath("$.success").value(false));verifyNoInteractions(email);
        service.setTicketEmail("");
        http.perform(multipart("/api/feedback/ticket").requestAttr("sessionId","synthetic-session").param("content","synthetic").param("type","network"))
            .andExpect(jsonPath("$.success").value(false));verifyNoInteractions(email);verifyNoInteractions(database);
    }
}
