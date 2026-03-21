package cn.gdeiassistant.contract;

import cn.gdeiassistant.core.photograph.controller.PhotographController;
import cn.gdeiassistant.core.photograph.pojo.vo.PhotographCommentVO;
import cn.gdeiassistant.core.photograph.pojo.vo.PhotographVO;
import cn.gdeiassistant.core.photograph.service.PhotographService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Date;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class PhotographContractTest {

    @Mock
    private PhotographService photographService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        PhotographController controller = new PhotographController();
        ReflectionTestUtils.setField(controller, "photographService", photographService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void listPhotographs_returnsDataArrayWithExpectedFields() throws Exception {
        PhotographVO vo = new PhotographVO();
        vo.setId(1);
        vo.setTitle("sunset");
        vo.setContent("campus sunset");
        vo.setType(0);
        vo.setLikeCount(10);
        vo.setCommentCount(3);

        when(photographService.queryPhotographList(eq(0), eq(10), eq(1), any()))
                .thenReturn(List.of(vo));

        mockMvc.perform(get("/api/photograph/type/1/start/0/size/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].id").value(1))
                .andExpect(jsonPath("$.data[0].title").value("sunset"))
                .andExpect(jsonPath("$.data[0].content").value("campus sunset"))
                .andExpect(jsonPath("$.data[0].type").value(0))
                .andExpect(jsonPath("$.data[0].likeCount").value(10))
                .andExpect(jsonPath("$.data[0].commentCount").value(3));
    }

    @Test
    void getPhotographById_returnsDataObjectWithExpectedFields() throws Exception {
        PhotographVO vo = new PhotographVO();
        vo.setId(1);
        vo.setTitle("morning");
        vo.setContent("early bird");
        vo.setType(1);
        vo.setLikeCount(5);
        vo.setCommentCount(2);

        when(photographService.getPhotographById(eq(1), any())).thenReturn(vo);

        mockMvc.perform(get("/api/photograph/id/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.title").value("morning"))
                .andExpect(jsonPath("$.data.content").value("early bird"))
                .andExpect(jsonPath("$.data.type").value(1))
                .andExpect(jsonPath("$.data.likeCount").value(5))
                .andExpect(jsonPath("$.data.commentCount").value(2));
    }

    @Test
    void getPhotographComments_returnsDataArrayWithExpectedFields() throws Exception {
        PhotographCommentVO comment = new PhotographCommentVO();
        comment.setCommentId(10);
        comment.setComment("great shot");
        comment.setCreateTime(new Date(1700000000000L));

        when(photographService.queryPhotographCommentList(1)).thenReturn(List.of(comment));

        mockMvc.perform(get("/api/photograph/id/1/comment"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].commentId").value(10))
                .andExpect(jsonPath("$.data[0].comment").value("great shot"))
                .andExpect(jsonPath("$.data[0].createTime").exists());
    }
}
