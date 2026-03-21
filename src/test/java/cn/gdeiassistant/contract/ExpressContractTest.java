package cn.gdeiassistant.contract;

import cn.gdeiassistant.common.pojo.Entity.ExpressComment;
import cn.gdeiassistant.core.express.controller.ExpressController;
import cn.gdeiassistant.core.express.pojo.vo.ExpressVO;
import cn.gdeiassistant.core.express.service.ExpressService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Date;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ExpressContractTest {

    private MockMvc mockMvc;
    private ExpressService expressService;

    @BeforeEach
    void setUp() {
        expressService = mock(ExpressService.class);

        ExpressController controller = new ExpressController();
        ReflectionTestUtils.setField(controller, "expressService", expressService);

        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void listEndpointReturnsExpectedFields() throws Exception {
        when(expressService.queryExpressPage(0, 10, "test-session"))
                .thenReturn(List.of(mockExpressVO()));

        mockMvc.perform(get("/api/express/start/0/size/10")
                        .requestAttr("sessionId", "test-session"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].id").exists())
                .andExpect(jsonPath("$.data[0].nickname").exists())
                .andExpect(jsonPath("$.data[0].content").exists())
                .andExpect(jsonPath("$.data[0].selfGender").exists())
                .andExpect(jsonPath("$.data[0].personGender").exists())
                .andExpect(jsonPath("$.data[0].publishTime").exists())
                .andExpect(jsonPath("$.data[0].likeCount").exists())
                .andExpect(jsonPath("$.data[0].commentCount").exists());
    }

    @Test
    void detailEndpointReturnsExpectedFields() throws Exception {
        when(expressService.queryExpressById(1, "test-session"))
                .thenReturn(mockExpressVO());

        mockMvc.perform(get("/api/express/id/1")
                        .requestAttr("sessionId", "test-session"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").exists())
                .andExpect(jsonPath("$.data.nickname").exists())
                .andExpect(jsonPath("$.data.content").exists())
                .andExpect(jsonPath("$.data.selfGender").exists())
                .andExpect(jsonPath("$.data.personGender").exists())
                .andExpect(jsonPath("$.data.publishTime").exists())
                .andExpect(jsonPath("$.data.likeCount").exists())
                .andExpect(jsonPath("$.data.commentCount").exists());
    }

    @Test
    void commentEndpointReturnsExpectedFields() throws Exception {
        when(expressService.queryExpressComment(1))
                .thenReturn(List.of(mockExpressComment()));

        mockMvc.perform(get("/api/express/id/1/comment")
                        .requestAttr("sessionId", "test-session"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].id").exists())
                .andExpect(jsonPath("$.data[0].comment").exists())
                .andExpect(jsonPath("$.data[0].publishTime").exists());
    }

    private static ExpressVO mockExpressVO() {
        ExpressVO vo = new ExpressVO();
        vo.setId(1);
        vo.setNickname("testUser");
        vo.setContent("test content");
        vo.setSelfGender(0);
        vo.setPersonGender(1);
        vo.setPublishTime(new Date());
        vo.setLikeCount(5);
        vo.setCommentCount(3);
        return vo;
    }

    private static ExpressComment mockExpressComment() {
        ExpressComment comment = new ExpressComment();
        comment.setId(1);
        comment.setComment("nice");
        comment.setPublishTime(new Date());
        return comment;
    }
}
