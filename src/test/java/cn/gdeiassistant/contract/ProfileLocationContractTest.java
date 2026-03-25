package cn.gdeiassistant.contract;

import cn.gdeiassistant.common.exceptionhandler.GlobalRestExceptionHandler;
import cn.gdeiassistant.core.profile.service.UserProfileService;
import cn.gdeiassistant.core.userProfile.controller.ProfileController;
import cn.gdeiassistant.core.userProfile.controller.support.ProfileLocationValidator;
import cn.gdeiassistant.core.userProfile.service.ProfileLocalizationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ProfileLocationContractTest {

    @Mock
    private UserProfileService userProfileService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        ProfileController controller = new ProfileController();
        ReflectionTestUtils.setField(controller, "userProfileService", userProfileService);
        ReflectionTestUtils.setField(controller, "profileLocationValidator", new ProfileLocationValidator());
        ReflectionTestUtils.setField(controller, "profileLocalizationService", new ProfileLocalizationService());
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalRestExceptionHandler())
                .build();
    }

    @Test
    void updateLocation_nullRegion_returnsFalse() throws Exception {
        mockMvc.perform(post("/api/profile/location")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"region\":null}")
                        .requestAttr("sessionId", "test-session"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("不合法的国家/地区代码"));
    }

    @Test
    void updateLocation_invalidRegion_returnsFalse() throws Exception {
        mockMvc.perform(post("/api/profile/location")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"region\":\"INVALID\"}")
                        .requestAttr("sessionId", "test-session"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("不合法的国家/地区代码"));
    }

    @Test
    void updateHometown_nullRegion_returnsFalse() throws Exception {
        mockMvc.perform(post("/api/profile/hometown")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"region\":null}")
                        .requestAttr("sessionId", "test-session"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("不合法的国家/地区代码"));
    }

    @Test
    void updateHometown_invalidRegion_returnsFalse() throws Exception {
        mockMvc.perform(post("/api/profile/hometown")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"region\":\"INVALID\"}")
                        .requestAttr("sessionId", "test-session"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("不合法的国家/地区代码"));
    }

    @Test
    void getLocationList_returnsCodeOnlyTree() throws Exception {
        mockMvc.perform(get("/api/profile/locations").header("Accept-Language", "zh-Hant"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].code").isNotEmpty())
                .andExpect(jsonPath("$.data[0].name").doesNotExist())
                .andExpect(jsonPath("$.data[0].children").isArray());
    }
}
