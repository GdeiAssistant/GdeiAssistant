package cn.gdeiassistant.contract;

import cn.gdeiassistant.common.exceptionhandler.GlobalRestExceptionHandler;
import cn.gdeiassistant.core.dating.controller.DatingController;
import cn.gdeiassistant.core.dating.pojo.vo.DatingPickVO;
import cn.gdeiassistant.core.dating.pojo.vo.DatingProfileVO;
import cn.gdeiassistant.core.dating.service.DatingService;
import cn.gdeiassistant.common.exception.DatabaseException.DataNotExistException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class DatingContractTest {

    @Mock
    private DatingService datingService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        DatingController controller = new DatingController();
        ReflectionTestUtils.setField(controller, "datingService", datingService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalRestExceptionHandler())
                .build();
    }

    @Test
    void listProfiles_returnsDataArrayWithExpectedFields() throws Exception {
        DatingProfileVO vo = new DatingProfileVO();
        vo.setProfileId(1);
        vo.setNickname("nick");
        vo.setGrade(3);
        vo.setFaculty("CS");
        vo.setHometown("GZ");
        vo.setContent("looking for roommate");

        when(datingService.queryDatingProfile(0, 10, 0)).thenReturn(List.of(vo));
        when(datingService.getRoommateProfilePictureURL(1)).thenReturn("https://r2.example.com/dating/1.jpg");

        mockMvc.perform(get("/api/dating/profile/area/0/start/0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].profileId").value(1))
                .andExpect(jsonPath("$.data[0].nickname").value("nick"))
                .andExpect(jsonPath("$.data[0].grade").value(3))
                .andExpect(jsonPath("$.data[0].faculty").value("CS"))
                .andExpect(jsonPath("$.data[0].hometown").value("GZ"))
                .andExpect(jsonPath("$.data[0].content").value("looking for roommate"));
    }

    @Test
    void listProfiles_rejectsInvalidPagingOrArea() throws Exception {
        mockMvc.perform(get("/api/dating/profile/area/0/start/-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false));

        mockMvc.perform(get("/api/dating/profile/area/2/start/0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false));

        verifyNoInteractions(datingService);
    }

    @Test
    void getProfileById_returnsDataObjectWithProfileAndDetailFields() throws Exception {
        DatingProfileVO profile = new DatingProfileVO();
        profile.setProfileId(1);
        profile.setNickname("nick");
        profile.setContent("hello");

        when(datingService.queryDatingProfile(Integer.valueOf(1))).thenReturn(profile);
        when(datingService.getRoommateProfilePictureURL(1)).thenReturn("https://r2.example.com/dating/1.jpg");
        when(datingService.queryRoommatePick(eq(1), any())).thenReturn(null);

        mockMvc.perform(get("/api/dating/profile/id/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.profile.profileId").value(1))
                .andExpect(jsonPath("$.data.profile.nickname").value("nick"))
                .andExpect(jsonPath("$.data.profile.content").value("hello"))
                .andExpect(jsonPath("$.data.pictureURL").exists())
                .andExpect(jsonPath("$.data.isContactVisible").value(false))
                .andExpect(jsonPath("$.data.isPickNotAvailable").value(false));
    }

    @Test
    void getProfileById_hiddenProfileReturnsDataNotExistError() throws Exception {
        when(datingService.queryDatingProfile(Integer.valueOf(99)))
                .thenThrow(new DataNotExistException("该卖室友信息不存在"));

        mockMvc.perform(get("/api/dating/profile/id/99"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value(40401));
    }
}
