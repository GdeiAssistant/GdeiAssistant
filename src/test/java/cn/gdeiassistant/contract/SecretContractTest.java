package cn.gdeiassistant.contract;

import cn.gdeiassistant.common.exceptionhandler.GlobalRestExceptionHandler;
import cn.gdeiassistant.core.secret.controller.SecretController;
import cn.gdeiassistant.core.secret.pojo.vo.SecretCommentVO;
import cn.gdeiassistant.core.secret.pojo.vo.SecretVO;
import cn.gdeiassistant.core.secret.service.SecretService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Date;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class SecretContractTest {

    private MockMvc mockMvc;
    private SecretService secretService;

    @BeforeEach
    void setUp() {
        secretService = mock(SecretService.class);

        SecretController controller = new SecretController();
        ReflectionTestUtils.setField(controller, "secretService", secretService);

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalRestExceptionHandler())
                .build();
    }

    @Test
    void listEndpointReturnsExpectedFields() throws Exception {
        when(secretService.getSecretInfo(0, 10, "test-session"))
                .thenReturn(List.of(mockSecretVO()));

        mockMvc.perform(get("/api/secret/info/start/0/size/10")
                        .requestAttr("sessionId", "test-session"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].id").exists())
                .andExpect(jsonPath("$.data[0].content").exists())
                .andExpect(jsonPath("$.data[0].theme").exists())
                .andExpect(jsonPath("$.data[0].type").exists())
                .andExpect(jsonPath("$.data[0].timer").exists())
                .andExpect(jsonPath("$.data[0].state").exists())
                .andExpect(jsonPath("$.data[0].publishTime").exists())
                .andExpect(jsonPath("$.data[0].likeCount").exists())
                .andExpect(jsonPath("$.data[0].commentCount").exists());
    }

    @Test
    void profilePagedEndpointReturnsExpectedFields() throws Exception {
        when(secretService.getSecretInfo("test-session", 20, 10))
                .thenReturn(List.of(mockSecretVO()));

        mockMvc.perform(get("/api/secret/profile/start/20/size/10")
                        .requestAttr("sessionId", "test-session"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].id").exists())
                .andExpect(jsonPath("$.data[0].content").exists())
                .andExpect(jsonPath("$.data[0].likeCount").exists())
                .andExpect(jsonPath("$.data[0].commentCount").exists());
    }

    @Test
    void profilePagedEndpointCapsPageSizeAtFifty() throws Exception {
        when(secretService.getSecretInfo("test-session", 0, 50))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/secret/profile/start/0/size/100")
                        .requestAttr("sessionId", "test-session"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(secretService).getSecretInfo("test-session", 0, 50);
    }

    @Test
    void profilePagedEndpointRejectsLowerBoundViolations() throws Exception {
        mockMvc.perform(get("/api/secret/profile/start/-1/size/10")
                        .requestAttr("sessionId", "test-session"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false));

        mockMvc.perform(get("/api/secret/profile/start/0/size/-5")
                        .requestAttr("sessionId", "test-session"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false));

        mockMvc.perform(get("/api/secret/info/start/-1/size/10")
                        .requestAttr("sessionId", "test-session"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false));

        verifyNoInteractions(secretService);
    }

    @Test
    void detailEndpointReturnsExpectedFields() throws Exception {
        when(secretService.getSecretDetailInfo(1, "test-session"))
                .thenReturn(mockSecretVO());

        mockMvc.perform(get("/api/secret/id/1")
                        .requestAttr("sessionId", "test-session"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").exists())
                .andExpect(jsonPath("$.data.content").exists())
                .andExpect(jsonPath("$.data.theme").exists())
                .andExpect(jsonPath("$.data.type").exists())
                .andExpect(jsonPath("$.data.timer").exists())
                .andExpect(jsonPath("$.data.state").exists())
                .andExpect(jsonPath("$.data.publishTime").exists())
                .andExpect(jsonPath("$.data.likeCount").exists())
                .andExpect(jsonPath("$.data.commentCount").exists());
    }

    @Test
    void commentsEndpointReturnsExpectedFields() throws Exception {
        when(secretService.checkSecretInfoExist(1)).thenReturn(true);
        when(secretService.getSecretComments(1))
                .thenReturn(List.of(mockSecretCommentVO()));

        mockMvc.perform(get("/api/secret/id/1/comments")
                        .requestAttr("sessionId", "test-session"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].id").exists())
                .andExpect(jsonPath("$.data[0].comment").exists())
                .andExpect(jsonPath("$.data[0].avatarTheme").exists())
                .andExpect(jsonPath("$.data[0].publishTime").exists());
    }

    private static SecretVO mockSecretVO() {
        SecretVO vo = new SecretVO();
        vo.setId(1);
        vo.setContent("secret content");
        vo.setTheme(2);
        vo.setType(0);
        vo.setTimer(24);
        vo.setState(0);
        vo.setPublishTime(new Date());
        vo.setLikeCount(10);
        vo.setCommentCount(4);
        return vo;
    }

    private static SecretCommentVO mockSecretCommentVO() {
        SecretCommentVO vo = new SecretCommentVO();
        vo.setId(1);
        vo.setComment("interesting");
        vo.setAvatarTheme(5);
        vo.setPublishTime(new Date());
        return vo;
    }
}
