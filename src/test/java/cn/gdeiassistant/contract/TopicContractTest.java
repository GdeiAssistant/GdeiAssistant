package cn.gdeiassistant.contract;

import cn.gdeiassistant.common.exceptionhandler.GlobalRestExceptionHandler;
import cn.gdeiassistant.core.topic.controller.TopicController;
import cn.gdeiassistant.core.topic.pojo.dto.TopicPublishDTO;
import cn.gdeiassistant.core.topic.pojo.vo.TopicVO;
import cn.gdeiassistant.core.topic.service.TopicService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class TopicContractTest {

    private MockMvc mockMvc;
    private TopicService topicService;

    @BeforeEach
    void setUp() {
        topicService = mock(TopicService.class);

        TopicController controller = new TopicController();
        ReflectionTestUtils.setField(controller, "topicService", topicService);

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalRestExceptionHandler())
                .build();
    }

    @Test
    void listEndpointReturnsExpectedFields() throws Exception {
        when(topicService.queryTopic("test-session", 0, 10))
                .thenReturn(List.of(mockTopicVO()));

        mockMvc.perform(get("/api/topic/start/0/size/10")
                        .requestAttr("sessionId", "test-session"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].id").exists())
                .andExpect(jsonPath("$.data[0].topic").exists())
                .andExpect(jsonPath("$.data[0].content").exists())
                .andExpect(jsonPath("$.data[0].count").exists())
                .andExpect(jsonPath("$.data[0].publishTime").exists())
                .andExpect(jsonPath("$.data[0].likeCount").exists());
    }

    @Test
    void listEndpointRejectsLowerBoundViolations() throws Exception {
        mockMvc.perform(get("/api/topic/start/-1/size/10")
                        .requestAttr("sessionId", "test-session"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false));

        mockMvc.perform(get("/api/topic/profile/start/0/size/0")
                        .requestAttr("sessionId", "test-session"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false));

        verifyNoInteractions(topicService);
    }

    @Test
    void detailEndpointReturnsExpectedFields() throws Exception {
        when(topicService.queryTopicById(1, "test-session"))
                .thenReturn(mockTopicVO());

        mockMvc.perform(get("/api/topic/id/1")
                        .requestAttr("sessionId", "test-session"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").exists())
                .andExpect(jsonPath("$.data.topic").exists())
                .andExpect(jsonPath("$.data.content").exists())
                .andExpect(jsonPath("$.data.count").exists())
                .andExpect(jsonPath("$.data.publishTime").exists())
                .andExpect(jsonPath("$.data.likeCount").exists());
    }

    @Test
    void publishEndpointDerivesZeroCountWhenCountParamIsMissing() throws Exception {
        mockMvc.perform(post("/api/topic")
                        .requestAttr("sessionId", "test-session")
                        .param("topic", "campus")
                        .param("content", "topic content"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        ArgumentCaptor<TopicPublishDTO> captor = ArgumentCaptor.forClass(TopicPublishDTO.class);
        verify(topicService).addTopic(captor.capture(), eq("test-session"));
        assertEquals(0, captor.getValue().getCount());
    }

    @Test
    void publishEndpointDerivesCountFromImageKeysWhenCountParamIsMissing() throws Exception {
        TopicVO vo = mockTopicVO();
        vo.setId(5);
        when(topicService.addTopic(any(TopicPublishDTO.class), eq("test-session"))).thenReturn(vo);

        mockMvc.perform(post("/api/topic")
                        .requestAttr("sessionId", "test-session")
                        .param("topic", "campus")
                        .param("content", "topic content")
                        .param("imageKeys", "tmp/topic-1.jpg", "tmp/topic-2.jpg"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        ArgumentCaptor<TopicPublishDTO> captor = ArgumentCaptor.forClass(TopicPublishDTO.class);
        verify(topicService).addTopic(captor.capture(), eq("test-session"));
        assertEquals(2, captor.getValue().getCount());
        verify(topicService).moveTopicItemPictureFromTempObject(5, 1, "tmp/topic-1.jpg");
        verify(topicService).moveTopicItemPictureFromTempObject(5, 2, "tmp/topic-2.jpg");
    }

    @Test
    void publishEndpointRejectsBlankImageKeysBeforeService() throws Exception {
        mockMvc.perform(post("/api/topic")
                        .requestAttr("sessionId", "test-session")
                        .param("topic", "campus")
                        .param("content", "topic content")
                        .param("imageKeys", "tmp/topic-1.jpg", ""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false));

        verifyNoInteractions(topicService);
    }

    @Test
    void publishEndpointDerivesCountFromMultipartImagesWhenCountParamIsMissing() throws Exception {
        TopicVO vo = mockTopicVO();
        vo.setId(6);
        when(topicService.addTopic(any(TopicPublishDTO.class), eq("test-session"))).thenReturn(vo);

        mockMvc.perform(multipart("/api/topic")
                        .file(new MockMultipartFile("images", "topic-1.jpg", "image/jpeg", "one".getBytes(StandardCharsets.UTF_8)))
                        .file(new MockMultipartFile("images", "topic-2.jpg", "image/jpeg", "two".getBytes(StandardCharsets.UTF_8)))
                        .requestAttr("sessionId", "test-session")
                        .param("topic", "campus")
                        .param("content", "topic content"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        ArgumentCaptor<TopicPublishDTO> captor = ArgumentCaptor.forClass(TopicPublishDTO.class);
        verify(topicService).addTopic(captor.capture(), eq("test-session"));
        assertEquals(2, captor.getValue().getCount());
        verify(topicService).uploadTopicItemPicture(eq(6), eq(1), any(InputStream.class));
        verify(topicService).uploadTopicItemPicture(eq(6), eq(2), any(InputStream.class));
    }

    @Test
    void detailEndpointRejectsInvalidIdBeforeService() throws Exception {
        mockMvc.perform(get("/api/topic/id/0")
                        .requestAttr("sessionId", "test-session"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false));

        verifyNoInteractions(topicService);
    }

    @Test
    void likeEndpointAcceptsValidId() throws Exception {
        mockMvc.perform(post("/api/topic/id/5/like")
                        .requestAttr("sessionId", "test-session"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(topicService).likeTopic(5, "test-session");
    }

    @Test
    void likeEndpointRejectsInvalidIdBeforeService() throws Exception {
        mockMvc.perform(post("/api/topic/id/0/like")
                        .requestAttr("sessionId", "test-session"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false));

        verifyNoInteractions(topicService);
    }

    @Test
    void imageEndpointReturnsUrlForValidIndex() throws Exception {
        when(topicService.downloadTopicItemPicture(5, 1)).thenReturn("https://example.com/topic/5/1.jpg");

        mockMvc.perform(get("/api/topic/id/5/index/1/image")
                        .requestAttr("sessionId", "test-session"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value("https://example.com/topic/5/1.jpg"));

        verify(topicService).downloadTopicItemPicture(5, 1);
    }

    @Test
    void imageEndpointRejectsInvalidIdOrIndexBeforeService() throws Exception {
        mockMvc.perform(get("/api/topic/id/0/index/1/image")
                        .requestAttr("sessionId", "test-session"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false));

        mockMvc.perform(get("/api/topic/id/5/index/0/image")
                        .requestAttr("sessionId", "test-session"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false));

        mockMvc.perform(get("/api/topic/id/5/index/10/image")
                        .requestAttr("sessionId", "test-session"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false));

        verifyNoInteractions(topicService);
    }

    private static TopicVO mockTopicVO() {
        TopicVO vo = new TopicVO();
        vo.setId(1);
        vo.setTopic("campus life");
        vo.setContent("topic content");
        vo.setCount(3);
        vo.setPublishTime(new Date());
        vo.setLikeCount(7);
        return vo;
    }
}
