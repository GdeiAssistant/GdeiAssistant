package cn.gdeiassistant.contract;

import cn.gdeiassistant.common.exceptionhandler.GlobalRestExceptionHandler;
import cn.gdeiassistant.core.dating.controller.DatingController;
import cn.gdeiassistant.core.dating.pojo.dto.DatingPickSubmitDTO;
import cn.gdeiassistant.core.dating.pojo.dto.DatingPublishDTO;
import cn.gdeiassistant.core.dating.pojo.vo.DatingProfileVO;
import cn.gdeiassistant.core.dating.service.DatingService;
import cn.gdeiassistant.common.exception.DatabaseException.DataNotExistException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
    void updatePickStateAcceptsValidState() throws Exception {
        mockMvc.perform(post("/api/dating/pick/id/3")
                        .requestAttr("sessionId", "test-session")
                        .param("state", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(datingService).verifyRoommatePickViewAccess("test-session", 3);
        verify(datingService).updateRoommatePickState(3, 1);
    }

    @Test
    void updatePickStateRejectsInvalidStateBeforeService() throws Exception {
        mockMvc.perform(post("/api/dating/pick/id/3")
                        .requestAttr("sessionId", "test-session")
                        .param("state", "0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false));

        verifyNoInteractions(datingService);
    }

    @Test
    void updatePickStateRejectsInvalidIdBeforeService() throws Exception {
        mockMvc.perform(post("/api/dating/pick/id/0")
                        .requestAttr("sessionId", "test-session")
                        .param("state", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false));

        verifyNoInteractions(datingService);
    }

    @Test
    void updateProfileStateAcceptsValidState() throws Exception {
        mockMvc.perform(post("/api/dating/profile/id/4/state")
                        .requestAttr("sessionId", "test-session")
                        .param("state", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(datingService).verifyRoommateProfileOwner("test-session", 4);
        verify(datingService).updateRoommateProfileState(4, 1);
    }

    @Test
    void updateProfileStateRejectsInvalidStateBeforeService() throws Exception {
        mockMvc.perform(post("/api/dating/profile/id/4/state")
                        .requestAttr("sessionId", "test-session")
                        .param("state", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false));

        verifyNoInteractions(datingService);
    }

    @Test
    void updateProfileStateRejectsInvalidIdBeforeService() throws Exception {
        mockMvc.perform(post("/api/dating/profile/id/0/state")
                        .requestAttr("sessionId", "test-session")
                        .param("state", "1"))
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
    void getProfileByIdRejectsInvalidIdBeforeService() throws Exception {
        mockMvc.perform(get("/api/dating/profile/id/0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false));

        verifyNoInteractions(datingService);
    }

    @Test
    void getProfilePictureRejectsInvalidIdBeforeService() throws Exception {
        mockMvc.perform(get("/api/dating/profile/id/0/picture"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false));

        verifyNoInteractions(datingService);
    }

    @Test
    void getPickDetailRejectsInvalidIdBeforeService() throws Exception {
        mockMvc.perform(get("/api/dating/pick/id/0")
                        .requestAttr("sessionId", "test-session"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false));

        verifyNoInteractions(datingService);
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

    @Test
    void publishProfileAcceptsValidPayloadAndImageKey() throws Exception {
        when(datingService.addRoommateProfile(eq("test-session"), any(DatingPublishDTO.class)))
                .thenReturn(7);

        mockMvc.perform(post("/api/dating/profile")
                        .requestAttr("sessionId", "test-session")
                        .param("nickname", "小明")
                        .param("grade", "3")
                        .param("faculty", "计科")
                        .param("hometown", "广州")
                        .param("content", "想找饭搭子")
                        .param("qq", "123456")
                        .param("wechat", "wechat-id")
                        .param("area", "1")
                        .param("imageKey", "upload/dating/tmp.jpg"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        ArgumentCaptor<DatingPublishDTO> captor = ArgumentCaptor.forClass(DatingPublishDTO.class);
        verify(datingService).addRoommateProfile(eq("test-session"), captor.capture());
        DatingPublishDTO dto = captor.getValue();
        assertEquals("小明", dto.getNickname());
        assertEquals(3, dto.getGrade());
        assertEquals("计科", dto.getFaculty());
        assertEquals("广州", dto.getHometown());
        assertEquals("想找饭搭子", dto.getContent());
        assertEquals("123456", dto.getQq());
        assertEquals("wechat-id", dto.getWechat());
        assertEquals(1, dto.getArea());
        verify(datingService).movePictureFromTempObject(7, "upload/dating/tmp.jpg");
    }

    @Test
    void publishProfileRejectsMissingRequiredFieldsBeforeService() throws Exception {
        mockMvc.perform(post("/api/dating/profile")
                        .requestAttr("sessionId", "test-session")
                        .param("nickname", "")
                        .param("grade", "3")
                        .param("faculty", "计科")
                        .param("hometown", "广州")
                        .param("content", "想找饭搭子")
                        .param("area", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false));

        verifyNoInteractions(datingService);
    }

    @Test
    void publishProfileRejectsInvalidBoundsBeforeService() throws Exception {
        mockMvc.perform(post("/api/dating/profile")
                        .requestAttr("sessionId", "test-session")
                        .param("nickname", "小明")
                        .param("grade", "0")
                        .param("faculty", "计科")
                        .param("hometown", "广州")
                        .param("content", "想找饭搭子")
                        .param("area", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false));

        mockMvc.perform(post("/api/dating/profile")
                        .requestAttr("sessionId", "test-session")
                        .param("nickname", "小明")
                        .param("grade", "3")
                        .param("faculty", "计科")
                        .param("hometown", "广州")
                        .param("content", "想找饭搭子")
                        .param("area", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false));

        mockMvc.perform(post("/api/dating/profile")
                        .requestAttr("sessionId", "test-session")
                        .param("nickname", "小明")
                        .param("grade", "3")
                        .param("faculty", "计科")
                        .param("hometown", "广州")
                        .param("content", "想找饭搭子")
                        .param("qq", "x".repeat(16))
                        .param("area", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false));

        verifyNoInteractions(datingService);
    }

    @Test
    void addPickAcceptsValidPayload() throws Exception {
        mockMvc.perform(post("/api/dating/pick")
                        .requestAttr("sessionId", "test-session")
                        .param("profileId", "3")
                        .param("content", "一起吃饭吗"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        ArgumentCaptor<DatingPickSubmitDTO> captor = ArgumentCaptor.forClass(DatingPickSubmitDTO.class);
        verify(datingService).verifyRoommatePickRequestAccess("test-session", 3);
        verify(datingService).addRoommatePick(eq("test-session"), captor.capture());
        assertEquals(3, captor.getValue().getProfileId());
        assertEquals("一起吃饭吗", captor.getValue().getContent());
    }

    @Test
    void addPickRejectsInvalidProfileIdBeforeService() throws Exception {
        mockMvc.perform(post("/api/dating/pick")
                        .requestAttr("sessionId", "test-session")
                        .param("profileId", "0")
                        .param("content", "一起吃饭吗"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false));

        verifyNoInteractions(datingService);
    }

    @Test
    void addPickRejectsOversizeContentBeforeService() throws Exception {
        mockMvc.perform(post("/api/dating/pick")
                        .requestAttr("sessionId", "test-session")
                        .param("profileId", "3")
                        .param("content", "x".repeat(51)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false));

        verifyNoInteractions(datingService);
    }
}
